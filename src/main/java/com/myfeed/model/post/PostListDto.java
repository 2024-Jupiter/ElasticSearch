package com.myfeed.model.post;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostListDto {

    private Long id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createdAt;
}
