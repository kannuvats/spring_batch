//package com.batchprocessing.batch.config;
//
//import com.batchprocessing.batch.constant.CONSTANT;
//import com.batchprocessing.batch.model.Student;
//import lombok.AllArgsConstructor;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
//import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
//import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
//import org.springframework.batch.core.launch.support.RunIdIncrementer;
//import org.springframework.batch.item.database.StoredProcedureItemReader;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.RowMapper;
//
//import javax.servlet.http.HttpServletResponse;
//import javax.sql.DataSource;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.List;
//
//@Configuration
//@EnableBatchProcessing
//@AllArgsConstructor
//public class BatchConfigg {
//    private final JobBuilderFactory jobBuilderFactory;
//    private final StepBuilderFactory stepBuilderFactory;
//    private final DataSource dataSource;
//
//    @Bean
//    public Step step1(HttpServletResponse response) {
//        return stepBuilderFactory.get("step1")
//                .<Student, Student>chunk(10)
//                .reader(reader())
//                .writer(writer(response))
//                .build();
//    }
//
//    @Bean
//    public StoredProcedureItemReader<Student> reader() {
//        StoredProcedureItemReader<Student> storedProcedureItemReader = new StoredProcedureItemReader<>();
//        storedProcedureItemReader.setDataSource(dataSource);
//        storedProcedureItemReader.setProcedureName("selectAllStudents");
//        storedProcedureItemReader.setRowMapper(new RowMapper<Student>() {
//            @Override
//            public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
//                Student student = new Student();
//                student.setId(rs.getInt("id"));
//                student.setName(rs.getString("name"));
//                student.setStandard(rs.getString("standard"));
//                student.setRollno(rs.getInt("rollno"));
//                student.setSubject(rs.getString("subject"));
//                return student;
//            }
//        });
//        return storedProcedureItemReader;
//    }
//
//    @Bean
//    public ItemWriter<Student> writer(HttpServletResponse response) {
//        return new ItemWriter<Student>() {
//            @Override
//            public void write(List<? extends Student> items) throws Exception {
//                try (XSSFWorkbook workbook = new XSSFWorkbook();
//                     OutputStream outputStream = response.getOutputStream()) {
//
//                    Sheet sheet = workbook.createSheet("Students");
//
//                    // Create header row
//                    Row headerRow = sheet.createRow(0);
//                    headerRow.createCell(0).setCellValue("ID");
//                    headerRow.createCell(1).setCellValue("Name");
//                    headerRow.createCell(2).setCellValue("Standard");
//                    headerRow.createCell(3).setCellValue("Roll No");
//                    headerRow.createCell(4).setCellValue("Subject");
//
//                    // Populate the sheet with data
//                    int rowCount = 1;
//                    for (Student student : items) {
//                        Row row = sheet.createRow(rowCount++);
//                        row.createCell(0).setCellValue(student.getId());
//                        row.createCell(1).setCellValue(student.getName());
//                        row.createCell(2).setCellValue(student.getStandard());
//                        row.createCell(3).setCellValue(student.getRollno());
//                        row.createCell(4).setCellValue(student.getSubject());
//                    }
//
//                    // Set response headers
//                    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//                    response.setHeader("Content-Disposition", "attachment; filename=students.xlsx");
//
//                    // Write the workbook to the output stream
//                    workbook.write(outputStream);
//                    outputStream.flush();
//                } catch (IOException e) {
//                    throw new RuntimeException("Error writing Excel file", e);
//                }
//            }
//        };
//    }
//
//    @Bean
//    public Job exportStudentJob(HttpServletResponse response) {
//        return jobBuilderFactory.get("exportStudentJob")
//                .incrementer(new RunIdIncrementer())
//                .flow(step1(response))
//                .end()
//                .build();
//    }
//}







   /* @Bean
    public ItemWriter<Student> excelWriter() {
        return students -> {
            // Prepare the model for Excel generation
            Map<String, Object> model = new HashMap<>();
            model.put("Excel Name", "students");
            model.put("HEADERS", List.of("ID","NAME","STANDARD","ROLLNO","SUBJECT"));

            // Convert students to List<List<String>>
            List<List<String>> results = convertStudentsToResults((List<Student>) students);
            model.put("RESULTS", results);

            // Create the Excel workbook using the utility
            XSSFWorkbook workbook = new XSSFWorkbook();
            buildExcelDocument(workbook, model);

            // Write the workbook to the response's output stream
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                workbook.write(byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();

                // Set response headers and content type
                response.setHeader("Content-Disposition", "attachment; filename="+ model.get("Excel Name") +".xlsx");
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

                // Write the bytes to the response output stream
                try (OutputStream out = response.getOutputStream()) {
                    out.write(bytes);
                    out.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to write Excel file to response", e);
            }
        };
    }

    public void buildExcelDocument(Workbook workbook, Map<String, Object> model) {
        String sheetName = (String) model.get("Excel Name");
        List<String> headers = (List<String>) model.get("HEADERS");
        List<List<String>> results = (List<List<String>>) model.get("RESULTS");
        List<String> numericColumns = new ArrayList<>();

        if (model.containsKey("numericcolumns")) {
            numericColumns = (List<String>) model.get("numericcolumns");
        }

        Sheet sheet = workbook.createSheet(sheetName);
        sheet.setDefaultColumnWidth(15);

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Create header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(headers.get(i));
        }

        // Populate the sheet with student data
        int rowNum = 1;
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

//    private void buildExcelDocument(Workbook workbook, Map<String,Object> map) {
//        String sheetName = (String) map.get("Excel Name");
//        List<String> headers = (List<String>) map.get("HEADERS");
//        List<List<String>> students = (List<List<String>>)map.get("RESULTS");
//        List<String> numericColumns = new ArrayList<>();
//        if (map.containsKey("numericcolumns")) {
//            numericColumns = (List<String>) map.get("numericcolumns");
//        }
//
//        Sheet sheet = workbook.createSheet(sheetName);
//        sheet.setDefaultColumnWidth(15);
//
//        // Create header style
//        CellStyle headerStyle = workbook.createCellStyle();
//        headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
//        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//        Font headerFont = workbook.createFont();
//        headerFont.setBold(true);
//        headerStyle.setFont(headerFont);
//
//        Row headerRow = sheet.createRow(0);
//        for (int i = 0; i < headers.size(); i++) {
//            Cell cell = headerRow.createCell(i);
//            cell.setCellStyle(headerStyle);
//            cell.setCellValue(headers.get(i));
//        }
//
//
//
//        // Create header row
//        // createHeaderRow(sheet, headers);
//
//        // Populate the sheet with student data
//        populateStudentData(sheet, students,numericColumns);
//    }
//
////    private void createHeaderRow(Sheet sheet, List<String> headers) {
////        Row headerRow = sheet.createRow(0);
////        for (int i = 0; i < headers.size(); i++) {
////            headerRow.createCell(i).setCellValue(headers.get(i));
////        }
////    }
//
//    private void populateStudentData(Sheet sheet, List<List<String>> results,List<String> numericColumns) {
//        int rowNum = 1;
//        for (List<String> result : results) {
//            Row row = sheet.createRow(rowNum++);
//            for (int i = 0; i < result.size(); i++) {
//                Cell cell = row.createCell(i);
//                String value = result.get(i);
//
//                if (numericColumns.contains(headers.get(i))) {
//                    // Attempt to parse the value as a number
//                    try {
//                        double numericValue = Double.parseDouble(value);
//                        cell.setCellValue(numericValue);
//                    } catch (NumberFormatException e) {
//                        // Handle the case where the value is not a number
//                        cell.setCellValue(value);
//                    }
//                } else {
//                    cell.setCellValue(value);
//                }
//            }
//        }
//    }
//
//
////    private void writeWorkbookToResponse(Workbook workbook) {
////        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
////            workbook.write(byteArrayOutputStream);
////            byte[] bytes = byteArrayOutputStream.toByteArray();
////
////            // Set response headers and content type
////            response.setHeader("Content-Disposition", "attachment; filename=students.xlsx");
////            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
////
////            // Write the bytes to the response output stream
////            try (OutputStream out = response.getOutputStream()) {
////                out.write(bytes);
////                out.flush();
////            }
////        } catch (IOException e) {
////            throw new RuntimeException("Failed to write Excel file to response", e);
////        }
////    }

    private List<List<String>> convertStudentsToResults(List<Student> students) {
        return students.stream().map(student -> List.of(
                String.valueOf(student.getId()),
                student.getName(),
                student.getStandard(),
                String.valueOf(student.getRollno()),
                student.getSubject()
        )).collect(Collectors.toList());
    }*/