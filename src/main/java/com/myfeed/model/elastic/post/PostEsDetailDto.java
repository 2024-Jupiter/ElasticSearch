package com.myfeed.model.elastic.post;

import com.myfeed.model.post.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostEsDetailDto {
    private String id;
    private String nickname;
    private String title;
    private String content;
    private Category category;
    private int viewCount;
    private int likeCount;

    public PostEsDetailDto(PostEs postEs) {
        this.nickname = postEs.getNickname();
        this.title = postEs.getTitle();
        this.content = postEs.getContent();
        this.category = postEs.getCategory();
        this.viewCount = postEs.getViewCount();
        this.likeCount = postEs.getLikeCount();

    }
}
