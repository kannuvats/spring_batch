package com.batchprocessing.batch.config;

import com.batchprocessing.batch.model.Student;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.StoredProcedureItemReader;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.RowMapper;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.batchprocessing.batch.constant.CONSTANT.HEADER;


@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final JobRepository jobRepository;
    private final HttpServletResponse response;
    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<Student, Student>chunk(100)
                .reader(reader())
                .writer(writer())
                .build();
    }

    @Bean
    public StoredProcedureItemReader<Student> reader() {
        StoredProcedureItemReader<Student> storedProcedureItemReader = new StoredProcedureItemReader<>();
        storedProcedureItemReader.setDataSource(dataSource);

        storedProcedureItemReader.setProcedureName("selectAllStudents");

        storedProcedureItemReader.setRowMapper(new RowMapper<Student>() {
            @Override
            public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
                Student student = new Student();
                    System.out.println("Mapping row " + rowNum);
                    student.setId(rs.getInt("id"));
                    student.setName(rs.getString("name"));
                    student.setStandard(rs.getString("standard"));
                    student.setRollno(rs.getInt("rollno"));
                    student.setSubject(rs.getString("subject"));
                return student;
            }
        });

        return storedProcedureItemReader;

    }

    @Bean
    public FlatFileItemWriter<Student> writer() {
        FlatFileItemWriter<Student> studentFlatFileItemWriter = new FlatFileItemWriter<>();
        studentFlatFileItemWriter.setResource(new FileSystemResource("student.csv"));
        studentFlatFileItemWriter.setHeaderCallback(new FlatFileHeaderCallback() {
            @Override
            public void writeHeader(Writer writer) throws IOException {
                writer.write(HEADER);
            }
        });
        studentFlatFileItemWriter.setLineAggregator(lineAggregator());
        return studentFlatFileItemWriter;
    }

    @Bean
    public DelimitedLineAggregator<Student> lineAggregator() {
        DelimitedLineAggregator<Student> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        BeanWrapperFieldExtractor<Student> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"id","name", "standard", "rollno", "subject"});
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

    @Bean
    public Job exportStudentJob(HttpServletResponse response) {
        return jobBuilderFactory.get("exportStudentJob")
                .incrementer(new RunIdIncrementer())
                .flow(step1())
                .end()
                .build();
    }

//    public static void downloadDocuments(HttpServletResponse httpServletResponse, Map<String, Object> map, XSSFWorkbook workbook) {
//        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
//            workbook.write(byteArrayOutputStream);
//            byte[] bytes = byteArrayOutputStream.toByteArray();
//            httpServletResponse.setHeader("Content-Disposition", "attachment; filename=" + getFileName(map) + ".xlsx");
//            httpServletResponse.setContentType("application/xlsx");
//            httpServletResponse.getOutputStream().write(bytes);
//        } catch (Exception exception) {
//            System.out.println("Exception occurs while downloading IBL Excel"+ exception.getMessage());
//        }
//    }


//    @Bean
//    public JobRepository jobRepository(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
//        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
//        factory.setDataSource(dataSource);
//        factory.setTransactionManager(transactionManager);
//        factory.setIsolationLevelForCreate("ISOLATION_SERIALIZABLE");
//        factory.setMaxVarCharLength(100);
//        return factory.getObject();
//    }
//
//    @Bean
//    public PlatformTransactionManager transactionManager(DataSource dataSource) {
//        return new DataSourceTransactionManager(dataSource);
//    }


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

