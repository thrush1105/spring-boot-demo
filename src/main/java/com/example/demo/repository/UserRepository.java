package com.example.demo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.dto.UserCreationDto;
import com.example.demo.model.User;

@Mapper
public interface UserRepository {

    List<User> findAll();

    User findByEmail(@Param("email") String email);

    void save(UserCreationDto dto);

}
