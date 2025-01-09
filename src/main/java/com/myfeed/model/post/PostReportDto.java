package com.myfeed.model.post;

import com.myfeed.model.user.UserDto;
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
public class PostReportDto {
    private Long id;
    private String title;
    private String content;
    private Category category;
    private UserDto userDto;
    // 이미지 업로드 시 필요
    //private List<String> images = new ArrayList<>();
    private List<ImageDto> images = new ArrayList<>();
    private int viewCount;
    private int likeCount;
    private BlockStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostReportDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.category = post.getCategory();
        this.userDto = new UserDto(post.getUser());
        // 이미지 업로드 시 필요
        /*
        this.images = post.getImages().stream()
                .map(Image::getImageSrc).toList();
         */
        this.images = post.getImages().stream()
                .map(ImageDto::new)
                .toList();
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.status = post.getStatus();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }
}
