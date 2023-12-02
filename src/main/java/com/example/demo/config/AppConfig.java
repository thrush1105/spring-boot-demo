package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CsvMapper csvMapper() {
        return CsvMapper.builder()
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false) // カラムの自動ソートを無効にする。
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false) // 日付をタイムスタンプとして出力しない。
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE) // カラム名はスネークケースを使用する。
                .build();
    }

}
