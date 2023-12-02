package com.example.demo.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.ibatis.cursor.Cursor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.example.demo.model.Sample;
import com.example.demo.repository.SampleRepository;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

@Service
public class SampleService {

    private final SampleRepository sampleRepository;

    private final RestTemplate restTemplate;

    private final CsvMapper csvMapper;

    private final CsvSchema csvSchema;

    public SampleService(SampleRepository sampleRepository, RestTemplate restTemplate, CsvMapper csvMapper) {
        this.sampleRepository = sampleRepository;
        this.restTemplate = restTemplate;
        this.csvMapper = csvMapper;
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
