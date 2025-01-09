package com.myfeed.model.elastic.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.myfeed.model.post.Category;
import com.myfeed.model.reply.ReplyEs;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@ToString
@Document(indexName = "posts")
public class PostEs {
    @Id
    @Field(name = "id",type = FieldType.Keyword)
    private String id;

    // 글쓴이 정보
    @Field(type = FieldType.Keyword,name = "nickname")
    private String nickname;

    // 게시글 정보
    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Keyword)
    private Category category;

    @Field(type = FieldType.Integer)
    private int viewCount;

    @Field(type = FieldType.Integer)
    private int likeCount;

    @Field(type = FieldType.Integer)
    private int replyCount;

    @Field(type = FieldType.Nested)
    private List<ReplyEs> replies;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Field(type = FieldType.Date, format = {DateFormat.date_time}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

}

