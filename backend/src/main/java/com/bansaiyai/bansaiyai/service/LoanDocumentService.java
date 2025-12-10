package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.LoanDocumentResponse;
import com.bansaiyai.bansaiyai.entity.Collateral;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.entity.enums.CollateralType;
import com.bansaiyai.bansaiyai.repository.CollateralRepository;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing loan collateral documents
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LoanDocumentService {

    private final CollateralRepository collateralRepository;
    private final LoanRepository loanRepository;

    @Value("${app.upload.loan-documents-dir:/opt/bansaiyai/uploads/loan-documents}")
    private String loanDocumentsDir;

    @Value("${app.upload.max-file-size:5242880}") // 5MB default
    private long maxFileSize;

    private static final List<String> ALLOWED_FILE_TYPES = List.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png");

    private static final List<String> ALLOWED_FILE_EXTENSIONS = List.of(
            ".pdf", ".jpg", ".jpeg", ".png");

    /**
     * Upload collateral document for a loan
     */
    @Transactional
    public LoanDocumentResponse uploadDocument(
            Long loanId,
            MultipartFile file,
            String documentName,
            String description,
            String uploadedBy) {

        log.info("Uploading document for loan: {}, name: {}", loanId, documentName);

        // 1. Validate loan exists
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + loanId));

        // 2. Validate file
        validateFile(file);

        // 3. Save file to storage
        String savedFilename = saveDocument(file, loanId);

        // 4. Create collateral record
        Collateral collateral = Collateral.builder()
                .loan(loan)
                .collateralType(CollateralType.OTHER) // Default, can be updated
                .description(description != null ? description : documentName)
                .estimatedValue(BigDecimal.ZERO) // Placeholder
                .documentPath(savedFilename)
                .ownershipDocument(documentName)
                .isVerified(false)
                .isReleased(false)
                .build();

        collateral = collateralRepository.save(collateral);

        log.info("Document uploaded successfully: ID={}, Filename={}", collateral.getId(), savedFilename);

        return mapToResponse(collateral);
    }

    /**
     * Get all documents for a loan
     */
    @Transactional(readOnly = true)
    public List<LoanDocumentResponse> getLoanDocuments(Long loanId) {
        // Validate loan exists
        if (!loanRepository.existsById(loanId)) {
            throw new IllegalArgumentException("Loan not found: " + loanId);
        }

        List<Collateral> collaterals = collateralRepository.findWithDocumentsByLoanId(loanId);

        return collaterals.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete a document
     */
    @Transactional
    public void deleteDocument(Long documentId, String deletedBy) {
        log.info("Deleting document: ID={}, DeletedBy={}", documentId, deletedBy);

        Collateral collateral = collateralRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        // Delete physical file
        if (collateral.getDocumentPath() != null) {
            deletePhysicalFile(collateral.getDocumentPath());
        }

        // Delete database record
        collateralRepository.delete(collateral);

        log.info("Document deleted successfully: ID={}", documentId);
    }

    /**
     * Get document by ID
     */
    @Transactional(readOnly = true)
    public LoanDocumentResponse getDocument(Long documentId) {
        Collateral collateral = collateralRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        return mapToResponse(collateral);
    }

    /**
     * Get document by UUID (for API endpoints - secure)
     */
    @Transactional(readOnly = true)
    public LoanDocumentResponse getDocumentByUuid(UUID uuid) {
        Collateral collateral = collateralRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        return mapToResponse(collateral);
    }

    /**
     * Delete document by UUID (for API endpoints - secure)
     */
    @Transactional
    public void deleteDocumentByUuid(UUID uuid, String deletedBy) {
        log.info("Deleting document: UUID={}, DeletedBy={}", uuid, deletedBy);

        Collateral collateral = collateralRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        // Delete physical file
        if (collateral.getDocumentPath() != null) {
            deletePhysicalFile(collateral.getDocumentPath());
        }

        // Delete database record
        collateralRepository.deleteByUuid(uuid);

        log.info("Document deleted successfully: UUID={}", uuid);
    }

    // ============================================================================
    // Private Helper Methods
    // ============================================================================

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum limit of %d MB",
                            maxFileSize / 1024 / 1024));
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_FILE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types: PDF, JPG, PNG");
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        // Security: Prevent path traversal in filename
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename - contains illegal characters");
        }

        int lastDot = filename.lastIndexOf(".");
        if (lastDot == -1) {
            throw new IllegalArgumentException("File must have an extension");
        }

        String extension = filename.substring(lastDot).toLowerCase();
        if (!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Invalid file extension. Allowed extensions: .pdf, .jpg, .jpeg, .png");
        }

        // Security: Validate actual file content matches extension
        // This prevents malicious files disguised with safe extensions
        try {
            byte[] fileBytes = file.getBytes();
            if (fileBytes.length < 4) {
                throw new IllegalArgumentException("File is too small to be valid");
            }

            // Check file magic numbers (file signatures)
            if (!isValidFileSignature(fileBytes, extension)) {
                log.warn("File signature mismatch for file: {}, extension: {}", filename, extension);
                throw new IllegalArgumentException(
                        "File content does not match extension. Possible file type mismatch.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read file content: " + e.getMessage());
        }
    }

    /**
     * Validate file signature (magic numbers) matches extension
     */
    private boolean isValidFileSignature(byte[] fileBytes, String extension) {
        // PDF signature: %PDF
        if (extension.equals(".pdf")) {
            return fileBytes.length >= 4 &&
                    fileBytes[0] == 0x25 && fileBytes[1] == 0x50 &&
                    fileBytes[2] == 0x44 && fileBytes[3] == 0x46;
        }

        // JPEG signature: FF D8 FF
        if (extension.equals(".jpg") || extension.equals(".jpeg")) {
            return fileBytes.length >= 3 &&
                    (fileBytes[0] & 0xFF) == 0xFF &&
                    (fileBytes[1] & 0xFF) == 0xD8 &&
                    (fileBytes[2] & 0xFF) == 0xFF;
        }

        // PNG signature: 89 50 4E 47 0D 0A 1A 0A
        if (extension.equals(".png")) {
            return fileBytes.length >= 8 &&
                    (fileBytes[0] & 0xFF) == 0x89 &&
                    (fileBytes[1] & 0xFF) == 0x50 &&
                    (fileBytes[2] & 0xFF) == 0x4E &&
                    (fileBytes[3] & 0xFF) == 0x47 &&
                    (fileBytes[4] & 0xFF) == 0x0D &&
                    (fileBytes[5] & 0xFF) == 0x0A &&
                    (fileBytes[6] & 0xFF) == 0x1A &&
                    (fileBytes[7] & 0xFF) == 0x0A;
        }

        return false;
    }

    /**
     * Save document to file system
     */
    private String saveDocument(MultipartFile file, Long loanId) {
        try {
            // Create directory structure: /loan-documents/{loanId}/
            Path loanDir = Paths.get(loanDocumentsDir, loanId.toString());
            if (!Files.exists(loanDir)) {
                Files.createDirectories(loanDir);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".pdf";

            String filename = String.format("%s_%s%s",
                    UUID.randomUUID().toString(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
                    extension);

            // Save file
            Path filePath = loanDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path: {loanId}/{filename}
            return loanId + "/" + filename;

        } catch (IOException e) {
            log.error("Failed to save document", e);
            throw new RuntimeException("Failed to save document: " + e.getMessage());
        }
    }

    /**
     * Delete physical file from storage
     */
    private void deletePhysicalFile(String documentPath) {
        try {
            Path filePath = Paths.get(loanDocumentsDir, documentPath);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Physical file deleted: {}", documentPath);
            }
        } catch (IOException e) {
            log.error("Failed to delete physical file: {}", documentPath, e);
            // Don't throw exception - database record should still be deleted
        }
    }

    /**
     * Map Collateral entity to LoanDocumentResponse DTO
     */
    private LoanDocumentResponse mapToResponse(Collateral collateral) {
        String documentUrl = collateral.getDocumentPath() != null
                ? "/api/loans/documents/files/" + collateral.getDocumentPath()
                : null;

        return LoanDocumentResponse.builder()
                .documentUuid(collateral.getUuid().toString()) // Use UUID for security
                .documentName(collateral.getOwnershipDocument())
                .documentType(getFileExtension(collateral.getDocumentPath()))
                .documentUrl(documentUrl)
                .fileSize(getFileSize(collateral.getDocumentPath()))
                .uploadedBy(collateral.getCreatedBy())
                .uploadedAt(collateral.getCreatedAt() != null
                        ? collateral.getCreatedAt().toString()
                        : null)
                .description(collateral.getDescription())
                .build();
    }

    /**
     * Get file extension from path
     */
    private String getFileExtension(String documentPath) {
        if (documentPath == null) {
            return null;
        }
        int lastDot = documentPath.lastIndexOf('.');
        return lastDot > 0 ? documentPath.substring(lastDot + 1).toUpperCase() : null;
    }

    /**
     * Get file size from storage
     */
    private Long getFileSize(String documentPath) {
        if (documentPath == null) {
            return null;
        }
        try {
            Path filePath = Paths.get(loanDocumentsDir, documentPath);
            if (Files.exists(filePath)) {
                return Files.size(filePath);
            }
        } catch (IOException e) {
            log.warn("Failed to get file size for: {}", documentPath);
        }
        return null;
    }

    /**
     * Get physical file path for serving
     */
    public Path getDocumentPath(String documentPath) {
        return Paths.get(loanDocumentsDir, documentPath).normalize();
    }

    /**
     * Verify that a loan belongs to a specific member
     * Used for access control
     */
    public boolean verifyLoanOwnership(Long loanId, Long memberId) {
        return loanRepository.findById(loanId)
                .map(loan -> loan.getMember().getId().equals(memberId))
                .orElse(false);
    }
}
