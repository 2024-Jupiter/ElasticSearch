package com.myfeed.model.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDetailDto {
    private String nickname;
    private String title;
    private String content;
    private Category category;
    private List<String> images = new ArrayList<>();
    private int viewCount;
    private int likeCount;
    private LocalDateTime earliestTime;

    public PostDetailDto(Post post) {
        this.nickname = post.getUser().getNickname();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.category = post.getCategory();
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.earliestTime = post.getCreatedAt().isBefore(post.getUpdatedAt())
                ? post.getCreatedAt()
                : post.getUpdatedAt();

        this.images = post.getImages().stream()
                .map(Image::getImageSrc).toList();
    }
}
