package com.batchprocessing.batch.config;

import com.batchprocessing.batch.model.Student;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class ExcelWriter {
    private final Workbook workbook;

    public void buildExcelDocument(Map<String, Object> model) {
        String sheetName = (String) model.get("Excel Name");
        List<String> headers = (List<String>) model.get("HEADERS");
        List<List<String>> results = (List<List<String>>) model.get("RESULTS");
        List<String> numericColumns = (List<String>) model.getOrDefault("numericcolumns", List.of());

        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
            sheet.setDefaultColumnWidth(15);
            createHeaderRow(sheet, headers);
        }

        // Populate the sheet with student data
        int rowNum = sheet.getPhysicalNumberOfRows(); // Get current row count
        for (List<String> result : results) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < result.size(); i++) {
                Cell cell = row.createCell(i);
                String value = result.get(i);

                if (numericColumns.contains(headers.get(i))) {
                    // Attempt to parse the value as a number
                    try {
                        double numericValue = Double.parseDouble(value);
                        cell.setCellValue(numericValue);
                    } catch (NumberFormatException e) {
                        // Handle the case where the value is not a number
                        cell.setCellValue(value);
                    }
                } else {
                    cell.setCellValue(value);
                }
            }
        }
    }

    public void createHeaderRow(Sheet sheet, List<String> headers) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(headers.get(i));
        }
    }
}
