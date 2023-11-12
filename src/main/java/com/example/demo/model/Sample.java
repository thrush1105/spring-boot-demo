package com.example.demo.model;

import java.sql.Date;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class Sample {

    private Long id;

    private String uuid;

    private String text;

    private Date date;

    private Timestamp createdAt;

    private Timestamp updatedAt;

}
