package com.example.demo.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.cursor.Cursor;

import com.example.demo.model.Sample;

@Mapper
public interface SampleRepository {

    Cursor<Sample> findAll(@Param("since") String since, @Param("until") String until);

}
