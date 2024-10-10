package com.batchprocessing.batch.assembler;

import com.batchprocessing.batch.model.Student;

import java.util.List;
import java.util.stream.Collectors;

public class ExcelAssembler {
    public List<List<String>> convertStudentsToResults(List<Student> students) {
        return students.stream().map(student -> List.of(
                String.valueOf(student.getId()),
                student.getName(),
                student.getStandard(),
                String.valueOf(student.getRollno()),
                student.getSubject()
        )).collect(Collectors.toList());
    }
}
