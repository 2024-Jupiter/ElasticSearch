package com.myfeed.service.Post.crawlingdata;

import com.myfeed.model.reply.ReplyEs;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VelogDto {

    private Long id;

    private String title;

    private String userName;

    private String content;

    private String date;
    private int likeCount;
    private List<VelogCommentDto> comments;

    public List<ReplyEs> velogCommentToReplyEs() {
        List<ReplyEs> replies = new ArrayList<>();
        if (comments != null && !comments.isEmpty()) {
            for (VelogCommentDto comment : comments) {
                replies.add(ReplyEs.builder()
                        .id(UUID.randomUUID().toString())
                        .nickname(comment.getCommentUser())
                        .content(comment.getCommentContent())
                        .createdAt(LocalDateTime.parse(comment.getCommentDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                        .build());
            }
        }
        return replies;
    }
}
