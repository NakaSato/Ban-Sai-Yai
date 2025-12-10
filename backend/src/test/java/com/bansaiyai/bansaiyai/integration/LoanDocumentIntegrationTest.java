package com.bansaiyai.bansaiyai.integration;

import com.bansaiyai.bansaiyai.entity.Collateral;
import com.bansaiyai.bansaiyai.entity.Loan;
import com.bansaiyai.bansaiyai.repository.CollateralRepository;
import com.bansaiyai.bansaiyai.repository.LoanRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Loan Document Management feature
 * Tests upload, download, list, and delete operations
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoanDocumentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CollateralRepository collateralRepository;

    @Autowired
    private LoanRepository loanRepository;

    private Loan testLoan;

    @BeforeEach
    void setUp() {
        // Set up test data
    }

    /**
     * Test 1: Complete document lifecycle
     * Upload → List → Get → Delete
     */
    @Test
    @WithMockUser(username = "officer1", roles = { "OFFICER" })
    void testCompleteDocumentLifecycle() throws Exception {
        // 1. Upload document
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "land_title.pdf",
                "application/pdf",
                createValidPdfBytes());

        MvcResult uploadResult = mockMvc.perform(multipart("/api/loans/101/documents")
                .file(pdfFile)
                .param("doc_name", "Land Title Deed")
                .param("description", "Chanote for 2 rai land in Chiang Mai"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Document uploaded successfully"))
                .andExpect(jsonPath("$.document.documentUuid").exists())
                .andExpect(jsonPath("$.document.documentName").value("Land Title Deed"))
                .andExpect(jsonPath("$.document.documentType").value("PDF"))
                .andReturn();

        // Extract UUID
        String responseBody = uploadResult.getResponse().getContentAsString();
        String documentUuid = JsonPath.read(responseBody, "$.document.documentUuid");

        // Verify file was saved
        Collateral collateral = collateralRepository.findByUuid(UUID.fromString(documentUuid))
                .orElseThrow();
        assertThat(collateral.getDocumentPath()).isNotNull();
        assertThat(collateral.getOwnershipDocument()).isEqualTo("Land Title Deed");

        // 2. List documents for loan
        mockMvc.perform(get("/api/loans/101/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.documents[?(@.documentUuid == '" + documentUuid + "')]").exists());

        // 3. Get specific document
        mockMvc.perform(get("/api/loans/documents/" + documentUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentUuid").value(documentUuid))
                .andExpect(jsonPath("$.documentName").value("Land Title Deed"))
                .andExpect(jsonPath("$.documentUrl").exists());

        // 4. Download document file
        mockMvc.perform(get(collateral.getDocumentPath()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));

        // 5. Delete document
        mockMvc.perform(delete("/api/loans/documents/" + documentUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Document deleted successfully"))
                .andExpect(jsonPath("$.documentUuid").value(documentUuid));

        // 6. Verify deletion
        mockMvc.perform(get("/api/loans/documents/" + documentUuid))
                .andExpect(status().isNotFound());

        assertThat(collateralRepository.findByUuid(UUID.fromString(documentUuid))).isEmpty();
    }

    /**
     * Test 2: File type validation - PDF
     */
    @Test
    @WithMockUser(username = "officer1", roles = { "OFFICER" })
    void testPdfFileValidation() throws Exception {
        MockMultipartFile validPdf = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", createValidPdfBytes());

        mockMvc.perform(multipart("/api/loans/101/documents")
                .file(validPdf)
                .param("doc_name", "Valid PDF"))
                .andExpect(status().isCreated());
    }

    /**
     * Test 3: File type validation - JPEG
     */
    @Test
    @WithMockUser(username = "officer1", roles = { "OFFICER" })
    void testJpegFileValidation() throws Exception {
        MockMultipartFile validJpeg = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", createValidJpegBytes());

        mockMvc.perform(multipart("/api/loans/101/documents")
                .file(validJpeg)
                .param("doc_name", "Property Photo"))
                .andExpect(status().isCreated());
    }

    /**
     * Test 4: File type validation - PNG
     */
    @Test
    @WithMockUser(username = "officer1", roles = { "OFFICER" })
    void testPngFileValidation() throws Exception {
        MockMultipartFile validPng = new MockMultipartFile(
                "file", "scan.png", "image/png", createValidPngBytes());

        mockMvc.perform(multipart("/api/loans/101/documents")
                .file(validPng)
                .param("doc_name", "Document Scan"))
                .andExpect(status().isCreated());
    }

    /**
     * Test 5: File signature mismatch detection
     */
    @Test
    @WithMockUser(username = "officer1", roles = { "OFFICER" })
    void testFileSignatureMismatch() throws Exception {
        // Fake PDF (text file with .pdf extension)
        MockMultipartFile fakePdf = new MockMultipartFile(
                "file",
                "malicious.pdf",
                "application/pdf",
                "This is not a real PDF file".getBytes());

        mockMvc.perform(multipart("/api/loans/101/documents")
                .file(fakePdf)
                .param("doc_name", "Fake Document"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("File content does not match extension")));
    }

    /**
     * Test 6: File size limit enforcement
     */
    @Test
    @WithMockUser(username = "officer1", roles = { "OFFICER" })
    void testFileSizeLimit() throws Exception {
        // Create 6MB file (exceeds 5MB limit)
        byte[] largeFile = new byte[6 * 1024 * 1024];
        MockMultipartFile oversizedFile = new MockMultipartFile(
                "file", "large.pdf", "application/pdf", largeFile);

        mockMvc.perform(multipart("/api/loans/101/documents")
                .file(oversizedFile)
                .param("doc_name", "Large Document"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("File size exceeds maximum limit")));
    }

    /**
     * Test 7: Path traversal prevention
     */
    @Test
    @WithMockUser(username = "officer1", roles = { "OFFICER" })
    void testPathTraversalPrevention() throws Exception {
        // Try to access file with path traversal
        mockMvc.perform(get("/api/loans/documents/files/101/../../../etc/passwd"))
                .andExpect(status().isBadRequest());

        // Try with backslash
        mockMvc.perform(get("/api/loans/documents/files/101/..\\..\\..\\windows\\system32\\config\\sam"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test 8: Member ownership verification
     */
    @Test
    @WithMockUser(username = "member1", roles = { "MEMBER" })
    void testMemberCanOnlyAccessOwnLoanDocuments() throws Exception {
        // Member tries to access another member's loan documents
        mockMvc.perform(get("/api/loans/999/documents"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(containsString("Access denied")));
    }

    /**
     * Test 9: Officer can access all documents
     */
    @Test
    @WithMockUser(username = "officer1", roles = { "OFFICER" })
    void testOfficerCanAccessAllDocuments() throws Exception {
        mockMvc.perform(get("/api/loans/101/documents"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/loans/999/documents"))
                .andExpect(status().isOk());
    }

    /**
     * Test 10: Invalid file extension rejection
     */
    @Test
    @WithMockUser(username = "officer1", roles = { "OFFICER" })
    void testInvalidFileExtensionRejection() throws Exception {
        MockMultipartFile exeFile = new MockMultipartFile(
                "file", "malware.exe", "application/octet-stream", "MZ".getBytes());

        mockMvc.perform(multipart("/api/loans/101/documents")
                .file(exeFile)
                .param("doc_name", "Malicious File"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Invalid file type")));
    }

    // Helper methods

    private byte[] createValidPdfBytes() {
        // PDF signature: %PDF
        return "%PDF-1.4\n%âãÏÓ\n".getBytes();
    }

    private byte[] createValidJpegBytes() {
        // JPEG signature: FF D8 FF
        return new byte[] {
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01
        };
    }

    private byte[] createValidPngBytes() {
        // PNG signature: 89 50 4E 47 0D 0A 1A 0A
        return new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52
        };
    }
}
