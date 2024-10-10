package com.batchprocessing.batch.config;

import com.batchprocessing.batch.model.Student;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentRowMapper implements RowMapper<Student> {

    @Override
    public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
        Student student = new Student();
        System.out.println("Mapping row " + rowNum);
        student.setId(rs.getInt("ID"));
        student.setName(rs.getString("NAME"));
        student.setStandard(rs.getString("STANDARD"));
        student.setRollno(rs.getInt("ROLLNO"));
        student.setSubject(rs.getString("SUBJECT"));
        return student;
    }
}
