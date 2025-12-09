package com.bansaiyai.bansaiyai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for exporting data to various formats (Excel, CSV).
 * Supports async export for large datasets.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataExportService {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /**
   * Export data to Excel format.
   *
   * @param sheetName Name of the Excel sheet
   * @param headers   Column headers
   * @param data      List of row data (each row is a list of values)
   * @return byte array of the Excel file
   */
  public byte[] exportToExcel(String sheetName, List<String> headers, List<List<Object>> data) {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

      Sheet sheet = workbook.createSheet(sheetName);

      // Create header style
      CellStyle headerStyle = createHeaderStyle(workbook);

      // Create header row
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < headers.size(); i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers.get(i));
        cell.setCellStyle(headerStyle);
      }

      // Create data rows
      CellStyle dateStyle = createDateStyle(workbook);
      CellStyle currencyStyle = createCurrencyStyle(workbook);

      int rowNum = 1;
      for (List<Object> rowData : data) {
        Row row = sheet.createRow(rowNum++);
        for (int i = 0; i < rowData.size(); i++) {
          Cell cell = row.createCell(i);
          setCellValue(cell, rowData.get(i), dateStyle, currencyStyle);
        }
      }

      // Auto-size columns
      for (int i = 0; i < headers.size(); i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(outputStream);
      return outputStream.toByteArray();

    } catch (IOException e) {
      log.error("Error exporting to Excel", e);
      throw new RuntimeException("Failed to export to Excel", e);
    }
  }

  /**
   * Async export for large datasets.
   */
  @Async("reportExecutor")
  public CompletableFuture<byte[]> exportToExcelAsync(String sheetName, List<String> headers,
      List<List<Object>> data) {
    log.info("Starting async Excel export for sheet: {}", sheetName);
    byte[] result = exportToExcel(sheetName, headers, data);
    log.info("Completed async Excel export for sheet: {} ({} bytes)", sheetName, result.length);
    return CompletableFuture.completedFuture(result);
  }

  /**
   * Export data to CSV format.
   */
  public byte[] exportToCsv(List<String> headers, List<List<Object>> data) {
    StringBuilder csv = new StringBuilder();

    // Add headers
    csv.append(String.join(",", headers)).append("\n");

    // Add data rows
    for (List<Object> row : data) {
      csv.append(row.stream()
          .map(this::formatCsvValue)
          .reduce((a, b) -> a + "," + b)
          .orElse(""))
          .append("\n");
    }

    return csv.toString().getBytes();
  }

  /**
   * Export multiple sheets to a single Excel file.
   */
  public byte[] exportMultiSheetExcel(Map<String, ExportSheet> sheets) {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

      CellStyle headerStyle = createHeaderStyle(workbook);
      CellStyle dateStyle = createDateStyle(workbook);
      CellStyle currencyStyle = createCurrencyStyle(workbook);

      for (Map.Entry<String, ExportSheet> entry : sheets.entrySet()) {
        Sheet sheet = workbook.createSheet(entry.getKey());
        ExportSheet exportSheet = entry.getValue();

        // Create header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < exportSheet.headers().size(); i++) {
          Cell cell = headerRow.createCell(i);
          cell.setCellValue(exportSheet.headers().get(i));
          cell.setCellStyle(headerStyle);
        }

        // Create data rows
        int rowNum = 1;
        for (List<Object> rowData : exportSheet.data()) {
          Row row = sheet.createRow(rowNum++);
          for (int i = 0; i < rowData.size(); i++) {
            Cell cell = row.createCell(i);
            setCellValue(cell, rowData.get(i), dateStyle, currencyStyle);
          }
        }

        // Auto-size columns
        for (int i = 0; i < exportSheet.headers().size(); i++) {
          sheet.autoSizeColumn(i);
        }
      }

      workbook.write(outputStream);
      return outputStream.toByteArray();

    } catch (IOException e) {
      log.error("Error exporting multi-sheet Excel", e);
      throw new RuntimeException("Failed to export to Excel", e);
    }
  }

  private CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setBorderBottom(BorderStyle.THIN);
    return style;
  }

  private CellStyle createDateStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));
    return style;
  }

  private CellStyle createCurrencyStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
    return style;
  }

  private void setCellValue(Cell cell, Object value, CellStyle dateStyle, CellStyle currencyStyle) {
    if (value == null) {
      cell.setCellValue("");
    } else if (value instanceof Number) {
      cell.setCellValue(((Number) value).doubleValue());
      if (value instanceof Double || value instanceof Float ||
          value instanceof java.math.BigDecimal) {
        cell.setCellStyle(currencyStyle);
      }
    } else if (value instanceof LocalDate) {
      cell.setCellValue(((LocalDate) value).format(DATE_FORMATTER));
      cell.setCellStyle(dateStyle);
    } else if (value instanceof LocalDateTime) {
      cell.setCellValue(((LocalDateTime) value).format(DATETIME_FORMATTER));
    } else if (value instanceof Boolean) {
      cell.setCellValue((Boolean) value);
    } else {
      cell.setCellValue(value.toString());
    }
  }

  private String formatCsvValue(Object value) {
    if (value == null)
      return "";
    String str = value.toString();
    // Escape quotes and wrap in quotes if contains comma, quote, or newline
    if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
      return "\"" + str.replace("\"", "\"\"") + "\"";
    }
    return str;
  }

  /**
   * Record for multi-sheet export.
   */
  public record ExportSheet(List<String> headers, List<List<Object>> data) {
  }
}
