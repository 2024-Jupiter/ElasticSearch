package com.myfeed.service.report;

import com.myfeed.exception.*;
import com.myfeed.model.post.BlockStatus;
import com.myfeed.model.post.Post;
import com.myfeed.model.reply.Reply;
import com.myfeed.model.report.ProcessStatus;
import com.myfeed.model.report.Report;
import com.myfeed.model.report.ReportDto;
import com.myfeed.model.report.ReportType;
import com.myfeed.repository.jpa.PostRepository;
import com.myfeed.repository.jpa.ReplyRepository;
import com.myfeed.repository.jpa.ReportRepository;
import com.myfeed.response.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    ReplyRepository replyRepository;
    @Autowired
    PostRepository postRepository;

    // 신고 불러오기
    @Override
    public Report findByReportId(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.REPORT_NOT_FOUND));
    }

    // 신고 게시글 내역 리스트 (동시성)
    @Override
    public Page<Report> getPagedReportsByPost(int page, Long postId) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("updatedAt").descending());
        Page<Report> reports = reportRepository.findPagedReportsByPost(postId, pageable);

        for (Report report: reports) {
            if (report.getPost().getUser() == null || report.getPost().getUser().isDeleted()) {
                throw new ExpectedException(ErrorCode.INCLUDED_DELETED_USER_POST_IN_REPORT);
            }
        }

        List<Report> filteredReports = reports.getContent().stream()
                .filter(report -> report.getPost().getUser() != null && !report.getPost().getUser().isDeleted())
                .toList();

        return new PageImpl<>(filteredReports, pageable, reports.getTotalElements());
    }

    // 신고된 게시글 리스트
    @Override
    public Page<Post> getReportedPosts(int page) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("updatedAt").descending());
        Page<Post> posts = reportRepository.findReportedPosts(pageable);

        for (Post post: posts) {
            if (post.getUser() == null || post.getUser().isDeleted()) {
                throw new ExpectedException(ErrorCode.INCLUDED_DELETED_USER_POST_IN_REPORT);
            }
        }

        List<Post> filteredPosts = posts.getContent().stream()
                .filter(post -> post.getUser() != null && !post.getUser().isDeleted())
                .toList();

        return new PageImpl<>(filteredPosts, pageable, posts.getTotalElements());
    }

    // 신고 댓글 내역 리스트 (동시성)
    @Override
    public Page<Report> getPagedReportsByReply(int page, Long replyId) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("updatedAt").descending());
        Page<Report> reports = reportRepository.findPagedReportsByReply(replyId, pageable);

        for (Report report: reports) {
            if (report.getReply().getUser() == null || report.getReply().getUser().isDeleted()) {
                throw new ExpectedException(ErrorCode.INCLUDED_DELETED_USER_REPLY_IN_REPORT);
            }
        }

        List<Report> filteredReports = reports.getContent().stream()
                .filter(report -> report.getReply().getUser() != null && !report.getReply().getUser().isDeleted())
                .toList();

        return new PageImpl<>(filteredReports, pageable, reports.getTotalElements());
    }

    // 신고된 댓글 리스트
    @Override
    public Page<Reply> getReportedReplies(int page) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("updatedAt").descending());
        Page<Reply> replies = reportRepository.findReportedReplies(pageable);

        for (Reply reply: replies) {
            if (reply.getUser() == null || reply.getUser().isDeleted()) {
                throw new ExpectedException(ErrorCode.INCLUDED_DELETED_USER_POST_IN_REPORT);
            }
        }

        List<Reply> filteredReplies = replies.getContent().stream()
                .filter(reply -> reply.getUser() != null && !reply.getUser().isDeleted())
                .toList();

        return new PageImpl<>(filteredReplies, pageable, replies.getTotalElements());
    }

    // 게시글 신고
    @Override
    public void reportPost(Long postId, ReportDto reportDto) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ExpectedException(ErrorCode.POST_NOT_FOUND));

        if (post.getUser() == null || post.getUser().isDeleted()) {
            throw new ExpectedException(ErrorCode.CAN_NOT_REPORT_DELETED_USER_POST);
        }
        if (post.getStatus() == BlockStatus.BLOCK_STATUS) {
            throw new ExpectedException(ErrorCode.ALREADY_BLOCKED_POST);
        }

        Report report = Report.builder()
                .post(post).type(ReportType.valueOf(reportDto.getType()))
                .description(reportDto.getDescription()).status(ProcessStatus.RELEASED)
                .build();

        reportRepository.save(report);
    }

    // 댓글 신고
    @Override
    public void reportReply(Long replyId, ReportDto reportDto) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new ExpectedException(ErrorCode.REPLY_NOT_FOUND));

        if (reply.getUser() == null || reply.getUser().isDeleted()) {
            throw new ExpectedException(ErrorCode.CAN_NOT_REPORT_DELETED_USER_REPLY);
        }
        if (reply.getStatus() == BlockStatus.BLOCK_STATUS) {
            throw new ExpectedException(ErrorCode.ALREADY_BLOCKED_REPLY);
        }

        Report report = Report.builder()
                .reply(reply).type(ReportType.valueOf(reportDto.getType()))
                .description(reportDto.getDescription()).status(ProcessStatus.RELEASED)
                .build();

        reportRepository.save(report);
    }

    // 게시글 차단
    @Override
    public void BlockPost(Long id) {
        Report report = findByReportId(id);
        Post post = report.getPost();

        if (post.getUser() == null || post.getUser().isDeleted()) {
            throw new ExpectedException(ErrorCode.CAN_NOT_REPORT_DELETED_USER_POST);
        }
        if (post.getStatus() == BlockStatus.BLOCK_STATUS) {
            throw new ExpectedException(ErrorCode.ALREADY_BLOCKED_POST);
        }
        if (report.getStatus() == ProcessStatus.COMPLETED) {
            throw new ExpectedException(ErrorCode.REPORT_COMPLETED);
        }

        post.setStatus(BlockStatus.BLOCK_STATUS);
        report.setStatus(ProcessStatus.COMPLETED);
        postRepository.save(post);
    }

    // 게시글 해제
    @Override
    public void unBlockPost(Long id) {
        Report report = findByReportId(id);
        Post post = report.getPost();

        if (post.getUser() == null || post.getUser().isDeleted()) {
            throw new ExpectedException(ErrorCode.CAN_NOT_REPORT_DELETED_USER_POST);
        }
        if (post.getStatus() == BlockStatus.NORMAL_STATUS) {
            throw new ExpectedException(ErrorCode.ALREADY_UNBLOCKED_POST);
        }
        if (report.getStatus() == ProcessStatus.PENDING) {
            throw new ExpectedException(ErrorCode.REPORT_PENDING);
        }

        post.setStatus(BlockStatus.NORMAL_STATUS);
        report.setStatus(ProcessStatus.RELEASED);
        postRepository.save(post);
    }

    // 댓글 차단
    @Override
    public void BlockReply(Long id) {
        Report report = findByReportId(id);
        Reply reply = report.getReply();

        if (reply.getUser() == null || reply.getUser().isDeleted()) {
            throw new ExpectedException(ErrorCode.CAN_NOT_REPORT_DELETED_USER_REPLY);
        }
        if (reply.getStatus() == BlockStatus.BLOCK_STATUS) {
            throw new ExpectedException(ErrorCode.ALREADY_BLOCKED_REPLY);
        }
        if (report.getStatus() == ProcessStatus.COMPLETED) {
            throw new ExpectedException(ErrorCode.REPORT_COMPLETED);
        }

        reply.setStatus(BlockStatus.BLOCK_STATUS);
        report.setStatus(ProcessStatus.COMPLETED);
        replyRepository.save(reply);
    }

    // 댓글 해제
    @Override
    public void unBlockReply(Long id) {
        Report report = findByReportId(id);
        Reply reply = report.getReply();

        if (reply.getUser() == null || reply.getUser().isDeleted()) {
            throw new ExpectedException(ErrorCode.CAN_NOT_REPORT_DELETED_USER_REPLY);
        }
        if (reply.getStatus() == BlockStatus.NORMAL_STATUS) {
            throw new ExpectedException(ErrorCode.ALREADY_UNBLOCKED_REPLY);
        }
        if (report.getStatus() == ProcessStatus.PENDING) {
            throw new ExpectedException(ErrorCode.REPORT_PENDING);
        }

        reply.setStatus(BlockStatus.NORMAL_STATUS);
        report.setStatus(ProcessStatus.RELEASED);
        replyRepository.save(reply);
    }
}
