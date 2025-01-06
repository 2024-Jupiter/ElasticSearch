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
    @Autowired private ReportRepository reportRepository;
    @Autowired private ReplyRepository replyRepository;
    @Autowired private PostRepository postRepository;

    // 신고 불러오기
    @Override
    public Report findByReportId(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.REPORT_NOT_FOUND));
    }

    // 신고 게시글 리스트 (동시성)
    @Override
    public Page<Report> getPagedReportsByPost(int page, Post post) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("updatedAt").descending());
        Page<Report> reports = reportRepository.findPagedReportsByPost(post, pageable);

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

    // 게시글 신고 내역 상세 보기
    @Override
    public List<Report> getReportsByPost(Long postId) {
        List<Report> reports = reportRepository.findReportByPostId(postId);

        for (Report report: reports) {
            if (report.getPost().getUser() == null || report.getPost().getUser().isDeleted()) {
                throw new ExpectedException(ErrorCode.INCLUDED_DELETED_USER_POST_IN_REPORT);
            }
        }

        return reports;
    }

    // 신고 댓글 리스트 (동시성)
    @Override
    public Page<Report> getPagedReportsByReply(int page, Reply reply) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("updatedAt").descending());
        Page<Report> reports = reportRepository.findPagedReportsByReply(reply, pageable);

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

    // 댓글 신고 내역 상세 보기
    @Override
    public List<Report> getReportsByReply(Long replyId) {
        List<Report> reports = reportRepository.findReportByReplyId(replyId);

        for (Report report: reports) {
            if (report.getReply().getUser() == null || report.getReply().getUser().isDeleted()) {
                throw new ExpectedException(ErrorCode.INCLUDED_DELETED_USER_REPLY_IN_REPORT);
            }
        }

        return reports;
    }

    // 게시글 신고
    @Override
    public Report reportPost(Long postId, ReportDto reportDto) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ExpectedException(ErrorCode.POST_NOT_FOUND));

        if (post.getUser() == null || post.getUser().isDeleted()) {
            throw new ExpectedException(ErrorCode.CAN_NOT_REPORT_DELETED_USER_POST);
        }
        if (post.getStatus() == BlockStatus.BLOCK_STATUS) {
            throw new ExpectedException(ErrorCode.ALREADY_BLOCKED_POST);
        }

        Report report = Report.builder()
                .post(post).type(ReportType.valueOf(reportDto.getType()))
                .description(reportDto.getDescription()).status(ProcessStatus.PENDING)
                .build();

        return reportRepository.save(report);
    }

    // 댓글 신고
    @Override
    public Report reportReply(Long replyId, ReportDto reportDto) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new ExpectedException(ErrorCode.REPLY_NOT_FOUND));

        if (reply.getUser() == null || reply.getUser().isDeleted()) {
            throw new ExpectedException(ErrorCode.CAN_NOT_REPORT_DELETED_USER_REPLY);
        }
        if (reply.getStatus() == BlockStatus.BLOCK_STATUS) {
            throw new ExpectedException(ErrorCode.ALREADY_BLOCKED_REPLY);
        }

        Report report = Report.builder()
                .reply(reply).type(ReportType.valueOf(reportDto.getType()))
                .description(reportDto.getDescription()).status(ProcessStatus.PENDING)
                .build();

        return reportRepository.save(report);
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
