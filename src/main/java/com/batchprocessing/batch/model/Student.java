package com.batchprocessing.batch.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "students")
public class Student {

    @Column(name = "id")
    @Id
    private Integer id;

    @Column(name = "rollno")
    private Integer rollno;

    @Column(name = "name")
    private String name;

    @Column(name = "standard")
    private String standard;

    @Column(name = "subject")
    private String subject;

}
