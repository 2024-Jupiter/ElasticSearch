package com.myfeed.model.reply;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReplyListDto {
    private Long id;
    private String content;
    private String author;
    private LocalDateTime createdAt;
}
