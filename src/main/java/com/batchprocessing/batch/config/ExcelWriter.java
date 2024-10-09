package com.batchprocessing.batch.config;

import com.batchprocessing.batch.model.Student;
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


public class ExcelWriter {

    @Bean
    public ItemWriter<Student> excelWriter(HttpServletResponse response) {
        return new ItemWriter<Student>() {
            @Override
            public void write(List<? extends Student> students) throws Exception {
                // Create a new workbook and sheet
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Students");

                // Create header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {"ID", "Name", "Standard", "Roll No", "Subject"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }

                // Populate the sheet with student data
                int rowNum = 1;
                for (Student student : students) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(student.getId());
                    row.createCell(1).setCellValue(student.getName());
                    row.createCell(2).setCellValue(student.getStandard());
                    row.createCell(3).setCellValue(student.getRollno());
                    row.createCell(4).setCellValue(student.getSubject());
                }

                // Write the workbook to a byte array output stream
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                    workbook.write(byteArrayOutputStream);
                    byte[] bytes = byteArrayOutputStream.toByteArray();

                    // Set response headers and content type
                    response.setHeader("Content-Disposition", "attachment; filename=students.xlsx");
                    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

                    // Write the bytes to the response output stream
                    response.getOutputStream().write(bytes);
                    response.getOutputStream().flush();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write Excel file to response", e);
                } finally {
                    workbook.close();
                }
            }
        };
    }
}
