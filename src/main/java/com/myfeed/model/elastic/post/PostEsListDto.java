package com.myfeed.model.elastic.post;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostEsListDto {

    private String id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createdAt;
}
