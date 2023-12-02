package com.example.demo.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.SampleService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("sample")
public class SampleController {

    private final SampleService sampleService;

    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @GetMapping("csv")
    public void index(
            @RequestParam(name = "since", required = false) String since,
            @RequestParam(name = "until", required = false) String until,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("text/csv");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename=\"sample.csv\"");

        PrintWriter writer = response.getWriter();

        sampleService.outputCsv(since, until, writer);
    }

    @GetMapping("file")
    public ResponseEntity<Resource> file(@RequestParam(name = "fileName") String fileName)
            throws IOException {
        Path path = Path.of(fileName);
        Resource resource = new PathResource(path);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/external-api")
    public Map<String, Object> callExternalApi() {
        return sampleService.callExternalApi();
    }

}
