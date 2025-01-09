package com.myfeed.repository.jpa;

import com.myfeed.model.post.Post;
import com.myfeed.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 내 게시글 페이지 네이션 (동시성)
    @Query("SELECT p FROM Post p WHERE p.user.isDeleted = false")
    Page<Post> findPagedPostsByUserId(User user, Pageable pageable);

    // 조회수 증가 (동시성)
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void updateViewCountById(@Param("id") Long id);

    // 좋아요 증가 (동시성)
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    void updateLikeCountById(@Param("id") Long id);

    // 좋아요 감소 (동시성)
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :id AND p.likeCount > 0")
    void decrementLikeCountById(@Param("id") Long id);


    Page<Post> findAll(Pageable pageable);
}