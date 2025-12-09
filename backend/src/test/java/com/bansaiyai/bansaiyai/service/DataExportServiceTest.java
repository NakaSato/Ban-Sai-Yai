package com.bansaiyai.bansaiyai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DataExportService.
 */
@ExtendWith(MockitoExtension.class)
class DataExportServiceTest {

  private DataExportService dataExportService;

  @BeforeEach
  void setUp() {
    dataExportService = new DataExportService();
  }

  @Test
  void exportToExcel_shouldCreateValidExcelFile() {
    // Arrange
    List<String> headers = Arrays.asList("ID", "Name", "Amount", "Date");
    List<List<Object>> data = Arrays.asList(
        Arrays.asList(1L, "John Doe", new BigDecimal("1000.50"), LocalDate.now()),
        Arrays.asList(2L, "Jane Smith", new BigDecimal("2500.00"), LocalDate.now().minusDays(1)));

    // Act
    byte[] result = dataExportService.exportToExcel("TestSheet", headers, data);

    // Assert
    assertNotNull(result);
    assertTrue(result.length > 0);
    // Excel files start with PK (ZIP signature)
    assertEquals((byte) 0x50, result[0]); // 'P'
    assertEquals((byte) 0x4B, result[1]); // 'K'
  }

  @Test
  void exportToCsv_shouldCreateValidCsvContent() {
    // Arrange
    List<String> headers = Arrays.asList("ID", "Name", "Amount");
    List<List<Object>> data = Arrays.asList(
        Arrays.asList(1L, "John Doe", 1000.50),
        Arrays.asList(2L, "Jane Smith", 2500.00));

    // Act
    byte[] result = dataExportService.exportToCsv(headers, data);
    String csvContent = new String(result);

    // Assert
    assertNotNull(result);
    assertTrue(csvContent.contains("ID,Name,Amount"));
    assertTrue(csvContent.contains("1,John Doe,1000.5"));
    assertTrue(csvContent.contains("2,Jane Smith,2500.0"));
  }

  @Test
  void exportToCsv_shouldEscapeSpecialCharacters() {
    // Arrange
    List<String> headers = Arrays.asList("Name", "Description");
    List<List<Object>> data = Arrays.asList(
        Arrays.asList("Test, Inc.", "Contains \"quotes\" and newlines\nhere"));

    // Act
    byte[] result = dataExportService.exportToCsv(headers, data);
    String csvContent = new String(result);

    // Assert
    assertTrue(csvContent.contains("\"Test, Inc.\""));
    assertTrue(csvContent.contains("\"Contains \"\"quotes\"\" and newlines"));
  }

  @Test
  void exportToExcel_shouldHandleNullValues() {
    // Arrange
    List<String> headers = Arrays.asList("ID", "Name", "Optional");
    List<List<Object>> data = Arrays.asList(
        Arrays.asList(1L, "Test", null));

    // Act
    byte[] result = dataExportService.exportToExcel("TestSheet", headers, data);

    // Assert
    assertNotNull(result);
    assertTrue(result.length > 0);
  }

  @Test
  void exportToExcel_shouldHandleDateTimeValues() {
    // Arrange
    List<String> headers = Arrays.asList("Date", "DateTime");
    List<List<Object>> data = Arrays.asList(
        Arrays.asList(LocalDate.of(2024, 1, 15), LocalDateTime.of(2024, 1, 15, 10, 30, 0)));

    // Act
    byte[] result = dataExportService.exportToExcel("TestSheet", headers, data);

    // Assert
    assertNotNull(result);
    assertTrue(result.length > 0);
  }

  @Test
  void exportToExcel_shouldHandleEmptyData() {
    // Arrange
    List<String> headers = Arrays.asList("ID", "Name");
    List<List<Object>> data = Arrays.asList();

    // Act
    byte[] result = dataExportService.exportToExcel("TestSheet", headers, data);

    // Assert
    assertNotNull(result);
    assertTrue(result.length > 0);
  }

  @Test
  void exportMultiSheetExcel_shouldCreateMultipleSheets() {
    // Arrange
    var sheets = java.util.Map.of(
        "Sheet1", new DataExportService.ExportSheet(
            Arrays.asList("A", "B"),
            Arrays.asList(Arrays.asList(1, 2))),
        "Sheet2", new DataExportService.ExportSheet(
            Arrays.asList("X", "Y"),
            Arrays.asList(Arrays.asList("a", "b"))));

    // Act
    byte[] result = dataExportService.exportMultiSheetExcel(sheets);

    // Assert
    assertNotNull(result);
    assertTrue(result.length > 0);
  }
}
