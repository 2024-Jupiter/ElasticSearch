package com.myfeed.service.Post.crawlingdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VelogCommentDto {

    private String commentUser;

    private String commentDate;

    private String commentContent;
}
