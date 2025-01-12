package com.myfeed.repository.jpa;

import com.myfeed.model.reply.Reply;
import com.myfeed.model.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import org.springframework.data.repository.query.Param;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    // 게시글 내의 댓글 페이지네이션 (동시성)
//    @Query("SELECT r FROM Reply r WHERE r.user.isDeleted = false")
//    Page<Reply> findPagedRepliesByPostId(Long postId, Pageable pageable);

    Page<Reply> findPagedRepliesByPostId(Long postId, Pageable pageable);




    // 게시글 내의 댓글 목록
    List<Reply> findByPostId(Long postId);
}
