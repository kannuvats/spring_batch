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
