package com.batchprocessing.batch.controller;

import com.batchprocessing.batch.response.ResponseHolder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@AllArgsConstructor
@RequestMapping("/batch")
@Slf4j
public class StudentController {
    private final JobLauncher jobLauncher;
    private final Job exportStudentJob;
    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @GetMapping("/download/students")
    public void exportStudents(@RequestParam(name = "schoolId") Long schoolId,HttpServletResponse response) throws Exception {
        ResponseHolder.setResponse(response);
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addLong("schoolId", schoolId)
                .toJobParameters();

        try {
            // Run the job
            JobExecution jobExecution = jobLauncher.run(exportStudentJob, jobParameters);
          //  jobExecution.getExecutionContext().put("response", response);
            // Check the job status and handle the output
            if (!(jobExecution.getStatus() == BatchStatus.COMPLETED)) {
                throw new RuntimeException("Job failed with status: " + jobExecution.getStatus());
            }
        } catch (Exception e) {
            log.error("Error during student export: {}", e.getMessage(), e);
            throw new RuntimeException("Export failed due to an unexpected error: " + e.getMessage(), e);
        }
    }
}