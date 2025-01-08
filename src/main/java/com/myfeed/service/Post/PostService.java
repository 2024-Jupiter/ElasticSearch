package com.myfeed.service.Post;

import com.myfeed.model.post.Post;
import com.myfeed.model.post.PostDto;
import com.myfeed.model.post.UpdateDto;
import com.myfeed.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PostService {
    public static final int PAGE_SIZE = 10;

    // 게시글 가져오기
    Post findPostById(Long id);

    // 내 게시글 페이지 네이션
    Page<Post> getPagedPostsByUserId(int page, User user);

    // 게시글 생성
    // void createPost(Long userId, PostDto postDto); // 이미지 x
    void createPost(Long userId, PostDto postDto) throws IOException;  // 이미지 o

    // 게시글 수정
    // void updatePost(Long id, User user, UpdateDto updateDto); // 이미지 x
    void updatePost(Long id, User user, UpdateDto updateDto) throws IOException;  // 이미지 o

    // 게시글 삭제
    void deletePostById(Long id, User user);

    // 조회수 증가 (동시성)
    void incrementPostViewCountById(Long id);

    // 좋아요 증가 (동시성)
    void incrementPostLikeCountById(Long id);

    // 좋아요 감소 (동시성)
    void decrementPostLikeCountById(Long id);
}
