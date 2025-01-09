package com.myfeed.service.Post;

import com.myfeed.model.elastic.post.PostEs;
import com.myfeed.model.post.Category;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class CsvFileReaderService {
    @Autowired private ResourceLoader resourceLoader;
    @Autowired private PostEsService postEsService;

    // ElasticSearch 저장
    // title	userName	date	content	likeCount	comments
    public void csvFileToElasticSearch() {
        try {
            Resource resource = resourceLoader.getResource("classpath:static/data/velog_data.csv");
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
                int count = 0;
                for (CSVRecord record: csvParser) {
                    String title = record.get("title");
                    String userName = record.get("userName");
                    String date = record.get("date");
                    String content = record.get("content");
                    String likeCount = record.get("likeCount");
                    //String comments = record.get("comments");

                    PostEs postEs = PostEs.builder()
                            .nickname(userName).title(title).content(content).category(Category.GENERAL)
                            .viewCount(0).likeCount(Integer.parseInt(likeCount)).createdAt(LocalDateTime.parse(date))
                            .build();

                    postEsService.syncToElasticsearch(postEs);

                    if (count ++ == 100)
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
