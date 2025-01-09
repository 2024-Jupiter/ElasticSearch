package com.myfeed.model.elastic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.myfeed.model.reply.ReplyEs;
import com.myfeed.model.post.Category;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostEsClientDto {

    private String id;
    private String title;
    private String content;
    private Category category;
    private int viewCount;
    private int likeCount;
    private int replyCount;
    private String nickname;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String createdAt;
    private List<ReplyEs> replies;
    private String _class;
    private Double score;
}
