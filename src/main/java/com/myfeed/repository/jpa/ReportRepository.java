package com.myfeed.repository.jpa;

import com.myfeed.model.post.Post;
import com.myfeed.model.reply.Reply;
import com.myfeed.model.report.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    // 신고 게시글 내역 페이지 네이션 (동시성)
    @Query("SELECT r FROM Report r WHERE r.post.user.isDeleted = false")
    Page<Report> findPagedReportsByPost(Long postId, Pageable pageable);

    // 신고된 게시글 페이지 네이션 (동시성)
    @Query("SELECT r.post FROM Report r WHERE r.post IS NOT NULL AND r.post.user.isDeleted = false")
    Page<Post> findReportedPosts(Pageable pageable);

    // 신고 댓글 내역 페이지 네이션 (동시성)
    @Query("SELECT r FROM Report r WHERE r.reply.user.isDeleted = false")
    Page<Report> findPagedReportsByReply(Long replyId, Pageable pageable);

    // 신고된 게시글 페이지 네이션 (동시성)
    @Query("SELECT r.reply FROM Report r WHERE r.reply IS NOT NULL AND r.reply.user.isDeleted = false")
    Page<Reply> findReportedReplies(Pageable pageable);
}
