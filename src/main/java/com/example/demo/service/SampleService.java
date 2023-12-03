package com.example.demo.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.apache.ibatis.cursor.Cursor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.example.demo.model.Sample;
import com.example.demo.repository.SampleRepository;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

@Service
public class SampleService {

    private final SampleRepository sampleRepository;

    private final RestTemplate restTemplate;

    private final CsvMapper csvMapper;

    private final CsvSchema csvSchema;

    public SampleService(SampleRepository sampleRepository, RestTemplate restTemplate,
            @Value("${spring.jackson.date-format:}") String dateFormat) {
        this.sampleRepository = sampleRepository;
        this.restTemplate = restTemplate;
        this.csvMapper = CsvMapper.builder()
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false) // カラムの自動ソートを無効にする。
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false) // 日付をタイムスタンプとして出力しない。
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE) // カラム名はスネークケースを使用する。
                .build();
        if (StringUtils.hasLength(dateFormat)) {
            this.csvMapper.setDateFormat(new SimpleDateFormat(dateFormat));
        }
        csvSchema = csvMapper.schemaFor(Sample.class)
                .rebuild()
                .setUseHeader(true) // ヘッダ行を出力する。
                .setQuoteChar(CsvSchema.DEFAULT_QUOTE_CHAR) // 囲み文字はダブルクォーテーションを使用する。
                .build();
    }

    @Transactional(readOnly = true)
    public void outputCsv(String since, String until, PrintWriter writer)
            throws IOException {
        try (Cursor<Sample> samples = sampleRepository.findAll(since, until)) {
            SequenceWriter csvWriter = csvMapper.writer(csvSchema).writeValues(writer);
            csvWriter.writeAll(samples);
        }
    }

    public Map<String, Object> callExternalApi() {
        String url = "https://httpbin.org/get";
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });
        return response.getBody();
    }

}
