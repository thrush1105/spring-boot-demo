package com.example.demo.model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class User {

    private Long id;

    private String email;

    private String name;

    @JsonIgnore
    private String password;

    private Timestamp createdAt;

    private Timestamp updatedAt;

}
