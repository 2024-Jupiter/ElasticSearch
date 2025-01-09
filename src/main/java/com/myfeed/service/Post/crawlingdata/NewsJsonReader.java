package com.myfeed.service.Post.crawlingdata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myfeed.service.Post.record.NewsDto;
import java.io.InputStream;
import java.util.List;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;

@NoArgsConstructor
public class NewsJsonReader {

    public List<NewsDto> loadJson() {
        try {
            // 파일 경로를 Spring Resource로 로드
            ClassPathResource resource = new ClassPathResource("static/data/NewsResult.json");

            // InputStream을 통해 파일 내용 읽기
            InputStream inputStream = resource.getInputStream();

            // Jackson ObjectMapper로 JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(inputStream,
                new TypeReference<List<NewsDto>>() {
                });
        }catch (Exception e) {
            throw new RuntimeException("Failed to read JSON file", e);
        }
    }
}
