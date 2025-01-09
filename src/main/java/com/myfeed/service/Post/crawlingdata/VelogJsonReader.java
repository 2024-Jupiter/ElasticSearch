package com.myfeed.service.Post.crawlingdata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;

@NoArgsConstructor
public class VelogJsonReader {
    public List<VelogDto> loadJson() {
        try {
            // 파일 경로를 Spring Resource로 로드
            ClassPathResource resource = new ClassPathResource("static/data/velog_data/velog_data.json");
//            ClassPathResource resource = new ClassPathResource("static/data/velog_data/velog_data_20250102.json");
//            ClassPathResource resource = new ClassPathResource("static/data/velog_data/velog_data_20250103_105924.json");

            // InputStream을 통해 파일 내용 읽기
            InputStream inputStream = resource.getInputStream();

            // Jackson ObjectMapper로 JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(inputStream,
                    new TypeReference<List<VelogDto>>() {
                    });
        }catch (Exception e) {
            throw new RuntimeException("Failed to read JSON file", e);
        }
    }
}
