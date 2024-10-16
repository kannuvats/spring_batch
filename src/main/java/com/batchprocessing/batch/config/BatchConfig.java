package com.batchprocessing.batch.config;

import com.batchprocessing.batch.assembler.ExcelAssembler;
import com.batchprocessing.batch.model.Student;
import com.batchprocessing.batch.response.ResponseHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.StoredProcedureItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.SqlParameter;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.sql.Types.INTEGER;

@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private XSSFWorkbook workbook; // Member variable for workbook
    private String sheetName;
    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;

    }

    @Bean
    public Job exportStudentJob() throws SQLException {
        return jobBuilderFactory.get("exportStudentJob")
                .incrementer(new RunIdIncrementer())
                .flow(step1())
                .end()
                .listener(jobExecutionListener())
                .build();
    }

    @Bean
    public Step step1() throws SQLException {
        return stepBuilderFactory.get("step1").<Student, Student>chunk(100).reader(reader(null)).writer(excelWriter()).build();
    }

    @Bean
    @StepScope
    public StoredProcedureItemReader<Student> reader(@Value("#{jobParameters['schoolId']}") Integer schoolId) throws SQLException {
        log.info("Initializing StoredProcedureItemReader with schoolId: {}", schoolId);
        StoredProcedureItemReader<Student> storedProcedureItemReader = new StoredProcedureItemReader<>();
        storedProcedureItemReader.setDataSource(dataSource);
        storedProcedureItemReader.setProcedureName("GetStudentsBySchoolId");
        SqlParameter[] parameters = new SqlParameter[1];
        parameters[0] = new SqlParameter("SCHOOL_ID", INTEGER);
        storedProcedureItemReader.setParameters(parameters);
        storedProcedureItemReader.setRowMapper(new StudentRowMapper());
        storedProcedureItemReader.setPreparedStatementSetter(ps -> ps.setInt(1, schoolId));
        return storedProcedureItemReader;
    }

    @Bean
    public ItemWriter<Student> excelWriter() {
        return students -> {
            Map<String, Object> model = new HashMap<>();
            model.put("Excel Name", "students");
            model.put("HEADERS", List.of("ID", "NAME", "STANDARD", "ROLLNO", "SUBJECT"));
            sheetName = (String) model.get("Excel Name");

            ExcelAssembler assembler = new ExcelAssembler();
            List<List<String>> results = assembler.convertStudentsToResults((List<Student>) students);
            model.put("RESULTS", results);

            ExcelWriter excelWriter = new ExcelWriter(workbook);
            excelWriter.buildExcelDocument(model);
        };
    }

    private void createBlankSheetWithHeaders() {
        try {
            workbook = new XSSFWorkbook();
            sheetName = "Students";
            Sheet sheet = workbook.createSheet(sheetName);
            List<String> headers = List.of("ID", "NAME", "STANDARD", "ROLLNO", "SUBJECT");
            ExcelWriter excelWriter = new ExcelWriter(workbook);
            excelWriter.createHeaderRow(sheet, headers);

        } catch (Exception e) {
            log.error("Error creating blank sheet: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create blank sheet", e);
        }
    }

    @Bean
    public JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                workbook = new XSSFWorkbook();
                log.info("Workbook created");
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                HttpServletResponse response = ResponseHolder.getResponse();
                for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
                    int count = stepExecution.getReadCount();
                    if (count == 0) {
                        createBlankSheetWithHeaders();
                        break;
                    }
                }

                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                    workbook.write(byteArrayOutputStream);
                    byte[] bytes = byteArrayOutputStream.toByteArray();

                    // Set response headers and content type
                    //  HttpServletResponse response = (HttpServletResponse) jobExecution.getJobParameters().get("response");
                    response.setHeader("Content-Disposition", "attachment; filename=" + sheetName + ".xlsx");
                    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

                    // Write the bytes to the response output stream
                    try (OutputStream out = response.getOutputStream()) {
                        out.write(bytes);
                        out.flush();
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write Excel file to response", e);
                } finally {
                    try {
                        workbook.close(); // Close the workbook
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }
}
