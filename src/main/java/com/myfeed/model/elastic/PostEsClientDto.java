package com.myfeed.model.elastic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.myfeed.model.elastic.post.PostEs;
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


    public PostEsClientDto(PostEs postEs) {
        this.id = postEs.getId();
        this.nickname = postEs.getNickname();
        this.title = postEs.getTitle();
        this.content = postEs.getContent();
        this.viewCount = postEs.getViewCount();
//        this.likeCount = postEs.getLikeCount();
        this.createdAt = String.valueOf(postEs.getCreatedAt());
    }
}
