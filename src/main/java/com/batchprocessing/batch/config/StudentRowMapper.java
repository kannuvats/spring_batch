package com.batchprocessing.batch.config;

import com.batchprocessing.batch.model.Student;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentRowMapper implements RowMapper<Student> {

    @Override
    public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
        Student student = new Student();
        student.setName(rs.getString("name"));
        student.setStandard(rs.getString("standard"));
        student.setRollno(rs.getInt("rollno"));
        student.setSubject(rs.getString("subject"));
        return student;
    }
}
