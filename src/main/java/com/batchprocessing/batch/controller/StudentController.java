package com.batchprocessing.batch.controller;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@AllArgsConstructor
@RequestMapping("/batch")
public class StudentController {
    private final JobLauncher jobLauncher;
    private final Job exportStudentJob;


    @GetMapping("/download/students")
    public ResponseEntity<String> downloadStudents(HttpServletResponse response) {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("someParam", "someValue")  // Add necessary parameters
                .toJobParameters();

        try {
            jobLauncher.run(exportStudentJob, jobParameters);
            return ResponseEntity.ok("Job started successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start job.");
        }

    }
}