package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.LoanDocumentResponse;
import com.bansaiyai.bansaiyai.service.LoanDocumentService;
import com.bansaiyai.bansaiyai.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.List;
import java.util.Map;

/**
 * REST controller for loan document management.
 * Handles collateral document uploads, listing, and deletion.
 */
@RestController
@RequestMapping("/api/loans")
@Slf4j
@RequiredArgsConstructor
public class LoanDocumentController {

    private final LoanDocumentService loanDocumentService;
    private final UserContext userContext;

    /**
     * Upload collateral document for a loan
     * POST /api/loans/{loanId}/documents
     * 
     * Access: Officer Only (during loan application process)
     */
    @PostMapping(value = "/{loanId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT')")
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long loanId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("doc_name") String docName,
            @RequestParam(value = "description", required = false) String description) {

        try {
            String uploadedBy = userContext.getCurrentUsername();

            LoanDocumentResponse response = loanDocumentService.uploadDocument(
                    loanId, file, docName, description, uploadedBy);

            log.info("Document uploaded for loan {}: ID={}, Name={}",
                    loanId, response.getDocumentUuid(), docName);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Document uploaded successfully",
                    "document", response));

        } catch (IllegalArgumentException e) {
            log.error("Validation error uploading document for loan {}: {}", loanId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading document for loan {}", loanId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload document: " + e.getMessage()));
        }
    }

    /**
     * Get all documents for a loan
     * GET /api/loans/{loanId}/documents
     * 
     * Access: President (for approval), Officer, Member (own loan)
     */
    @GetMapping("/{loanId}/documents")
    @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT') or hasRole('SECRETARY') or hasRole('MEMBER')")
    public ResponseEntity<?> getLoanDocuments(@PathVariable Long loanId) {
        try {
            // Security: Verify member can only access their own loan documents
            boolean isOfficer = userContext.hasRole("OFFICER") ||
                    userContext.hasRole("PRESIDENT") ||
                    userContext.hasRole("SECRETARY");

            if (!isOfficer) {
                // Member role - verify ownership
                Long currentMemberId = userContext.getCurrentMemberId();
                if (currentMemberId == null) {
                    log.warn("Member access attempt without member ID for loan {}", loanId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "Access denied"));
                }

                // Verify loan belongs to member
                if (!loanDocumentService.verifyLoanOwnership(loanId, currentMemberId)) {
                    log.warn("Unauthorized access attempt to loan {} by member {}",
                            loanId, currentMemberId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "Access denied - not your loan"));
                }
            }

            List<LoanDocumentResponse> documents = loanDocumentService.getLoanDocuments(loanId);

            return ResponseEntity.ok(Map.of(
                    "count", documents.size(),
                    "documents", documents));

        } catch (IllegalArgumentException e) {
            log.error("Validation error getting documents for loan {}: {}", loanId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting documents for loan {}", loanId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve documents"));
        }
    }

    /**
     * Get document by UUID (secure - prevents ID enumeration)
     * GET /api/loans/documents/{uuid}
     */
    @GetMapping("/documents/{uuid}")
    @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT') or hasRole('SECRETARY') or hasRole('MEMBER')")
    public ResponseEntity<?> getDocument(@PathVariable UUID uuid) {
        try {
            LoanDocumentResponse document = loanDocumentService.getDocumentByUuid(uuid);
            return ResponseEntity.ok(document);
        } catch (IllegalArgumentException e) {
            // Return 404 for both "not found" and "not authorized" to prevent information
            // leakage
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting document {}", uuid, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve document"));
        }
    }

    /**
     * Delete document by UUID (secure - prevents ID enumeration)
     * DELETE /api/loans/documents/{uuid}
     */
    @DeleteMapping("/documents/{uuid}")
    @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT') or hasRole('SECRETARY')")
    public ResponseEntity<?> deleteDocument(@PathVariable UUID uuid) {
        try {
            String deletedBy = userContext.getCurrentUsername();
            loanDocumentService.deleteDocumentByUuid(uuid, deletedBy);

            log.info("Document deleted: UUID={}, DeletedBy={}", uuid, deletedBy);

            return ResponseEntity.ok(Map.of(
                    "message", "Document deleted successfully",
                    "documentUuid", uuid.toString()));
        } catch (IllegalArgumentException e) {
            // Return 404 for both "not found" and "not authorized" to prevent information
            // leakage
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting document {}", uuid, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete document"));
        }
    }

    /**
     * Serve document file
     * GET /api/loans/documents/files/{loanId}/{filename}
     * 
     * This endpoint serves the actual file content
     */
    @GetMapping("/documents/files/{loanId}/{filename:.+}")
    @PreAuthorize("hasRole('OFFICER') or hasRole('PRESIDENT') or hasRole('SECRETARY') or hasRole('MEMBER')")
    public ResponseEntity<Resource> getDocumentFile(
            @PathVariable Long loanId,
            @PathVariable String filename) {
        try {
            // Security: Validate filename to prevent path traversal
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                log.warn("Path traversal attempt detected: loan={}, file={}", loanId, filename);
                return ResponseEntity.badRequest().build();
            }

            // Security: Validate file extension
            String lowerFilename = filename.toLowerCase();
            if (!lowerFilename.endsWith(".pdf") && !lowerFilename.endsWith(".jpg") &&
                    !lowerFilename.endsWith(".jpeg") && !lowerFilename.endsWith(".png")) {
                log.warn("Invalid file extension requested: {}", filename);
                return ResponseEntity.badRequest().build();
            }

            // Security: Verify member can only access their own loan documents
            boolean isOfficer = userContext.hasRole("OFFICER") ||
                    userContext.hasRole("PRESIDENT") ||
                    userContext.hasRole("SECRETARY");

            if (!isOfficer) {
                Long currentMemberId = userContext.getCurrentMemberId();
                if (currentMemberId == null || !loanDocumentService.verifyLoanOwnership(loanId, currentMemberId)) {
                    log.warn("Unauthorized file access attempt: loan={}, member={}",
                            loanId, currentMemberId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            // Construct document path
            String documentPath = loanId + "/" + filename;

            // Load file as Resource
            Path filePath = loanDocumentService.getDocumentPath(documentPath);

            // Security: Verify path is within allowed directory
            Path allowedDir = Paths.get("/opt/bansaiyai/uploads/loan-documents").normalize();
            if (!filePath.normalize().startsWith(allowedDir)) {
                log.warn("Path traversal attempt blocked: {}", documentPath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Determine content type
            String contentType = determineContentType(filename);

            // Security: Set security headers
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .header("X-Content-Type-Options", "nosniff")
                    .header("X-Frame-Options", "DENY")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error serving document file: {}/{}", loanId, filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Determine content type from filename
     */
    private String determineContentType(String filename) {
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".png")) {
            return "image/png";
        }
        return "application/octet-stream";
    }
}
