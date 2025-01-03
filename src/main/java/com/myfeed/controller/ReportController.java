package com.myfeed.controller;

import com.myfeed.model.post.Post;
import com.myfeed.model.post.PostReportDto;
import com.myfeed.model.reply.Reply;
import com.myfeed.model.reply.ReplyReportDto;
import com.myfeed.model.report.ProcessStatus;
import com.myfeed.model.report.Report;
import com.myfeed.model.report.ReportDetailDto;
import com.myfeed.model.report.ReportDto;
import com.myfeed.service.Post.PostService;
import com.myfeed.service.reply.ReplyService;
import com.myfeed.service.report.ReportService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Validated
@RequestMapping("/api/admin/reports")
public class ReportController {
    @Autowired ReportService reportService;
    @Autowired
    private ReplyService replyService;
    @Autowired
    private PostService postService;

    // 게시글 신고 폼 (GET 요청 으로 폼을 가져옴)
    @GetMapping("/posts/{postId}/form")
    public String reportPostForm(@PathVariable Long postId) {
        return "api/admin/reports/reportPost";
    }

    // 게시글 신고
    @ResponseBody
    @PostMapping("/posts/{postId}")
    public ResponseEntity<Map<String, Object>> reportPost(@PathVariable Long postId,
                                                          @Valid @RequestBody ReportDto reportDto) {
        Report report = reportService.reportPost(postId, reportDto);
        Map<String, Object> response = new HashMap<>();

        String redirectUrl = "/api/posts/detail/" + postId;
        response.put("redirectUrl",redirectUrl);
        response.put("success", true);
        response.put("message", "게시글이 신고 되었습니다.");
        response.put("data", report);

        return ResponseEntity.ok(response);
    }

    // 댓글 신고 폼 (GET 요청 으로 폼을 가져옴)
    @GetMapping("/replies/{replyId}/form")
    public String reportReplyForm(@PathVariable Long replyId) {
        return "api/admin/reports/reportReply";
    }

    // 댓글 신고
    @ResponseBody
    @PostMapping("/replies/{replyId}")
    public ResponseEntity<Map<String, Object>> reportReply(@PathVariable Long replyId,
                                                           @Valid @RequestBody ReportDto reportDto) {
        Report report = reportService.reportReply(replyId, reportDto);
        Map<String, Object> response = new HashMap<>();

        String redirectUrl = "/api/posts/detail/" + report.getPost().getId();
        response.put("redirectUrl",redirectUrl);
        response.put("success", true);
        response.put("message", "댓글이 신고 되었습니다.");
        response.put("data", report);

        return ResponseEntity.ok(response);
    }

    // 신고 게시글 리스트 페이지 네이션 (동시성)
    @ResponseBody
    @GetMapping("/posts/{postId}")
    public ResponseEntity<Map<String, Object>> getReportsByPost(@PathVariable Long postId,
                                                         @RequestParam(name="p", defaultValue = "1") int page,
                                                         @Valid @RequestParam(name="status", required = false) String status,
                                                         HttpSession session) {
        Post post = postService.findPostById(postId);
        Page<Report> reports = reportService.getPagedReportsByPost(page, post);
        Map<String, Object> response = new HashMap<>();

        if (status != null) {
            ProcessStatus processStatus = ProcessStatus.valueOf(status.toUpperCase());

            List<Report> filteredReports = reports.stream()
                    .filter(report -> report.getStatus() == processStatus)
                    .collect(Collectors.toList());

            reports = new PageImpl<>(filteredReports, reports.getPageable(), filteredReports.size());
        }

        int totalPages = reports.getTotalPages();
        int startPage = (int) Math.ceil((page - 0.5) / postService.PAGE_SIZE - 1) * postService.PAGE_SIZE + 1;
        int endPage = Math.min(startPage + postService.PAGE_SIZE - 1, totalPages);
        List<Integer> pageList = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageList.add(i);
        }

        session.setAttribute("currentPostPage", page);
        response.put("success", true);
        response.put("message", "신고 게시글 리스트");
        response.put("data", reports.getContent());
        response.put("totalPages", totalPages);
        response.put("startPage", startPage);
        response.put("endPage", endPage);
        response.put("pageList", pageList);

        return ResponseEntity.ok(response);
    }

    // 게시글 신고 내역 상세 보기
    @ResponseBody
    @GetMapping("/posts/{postId}/detail")
    public ResponseEntity<Map<String, Object>> getPostDetail(@PathVariable Long postId) {
        Post post = postService.findPostById(postId);
        PostReportDto postReportDto = new PostReportDto(post);

        List<Report> reports = reportService.getReportsByPost(postId);
        List<ReportDetailDto> reportDetailDto = reports.stream()
                .map(ReportDetailDto::new)
                .toList();


        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "게시글 상세 조회");
        response.put("post", postReportDto);
        response.put("reports", reportDetailDto);

        return ResponseEntity.ok(response);
    }

    // 신고 댓글 리스트 페이지 네이션 (동시성)
    @ResponseBody
    @GetMapping("/replies/{replyId}")
    public ResponseEntity<Map<String, Object>> getReportsByReply( @PathVariable Long replyId,
                                                           @RequestParam(name="p", defaultValue = "1") int page,
                                                           @Valid @RequestParam(name="status", required = false) String status,
                                                           HttpSession session) {
        Reply reply = replyService.findByReplyId(replyId);
        Page<Report> reports = reportService.getPagedReportsByReply(page, reply);
        Map<String, Object> response = new HashMap<>();

        if (status != null) {
            ProcessStatus processStatus = ProcessStatus.valueOf(status.toUpperCase());

            List<Report> filteredReports = reports.stream()
                    .filter(report -> report.getStatus() == processStatus)
                    .collect(Collectors.toList());

            reports = new PageImpl<>(filteredReports, reports.getPageable(), filteredReports.size());
        }

        int totalPages = reports.getTotalPages();
        int startPage = (int) Math.ceil((page - 0.5) / replyService.PAGE_SIZE - 1) * replyService.PAGE_SIZE + 1;
        int endPage = Math.min(startPage + replyService.PAGE_SIZE - 1, totalPages);
        List<Integer> pageList = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageList.add(i);
        }

        session.setAttribute("currentPostPage", page);
        response.put("success", true);
        response.put("message", "신고 댓글 리스트");
        response.put("data", reports.getContent());
        response.put("totalPages", totalPages);
        response.put("startPage", startPage);
        response.put("endPage", endPage);
        response.put("pageList", pageList);

        return ResponseEntity.ok(response);
    }

    // 댓글 신고 내역 상세 보기
    @ResponseBody
    @GetMapping("/replies/{replyId}/detail")
    public ResponseEntity<Map<String, Object>> getReplyDetail(@PathVariable Long replyId) {
        Reply reply = replyService.findByReplyId(replyId);
        ReplyReportDto reportDto = new ReplyReportDto(reply);

        List<Report> reports = reportService.getReportsByReply(replyId);
        List<ReportDetailDto> reportDetailDto = reports.stream()
                .map(ReportDetailDto::new)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "댓글 상세 조회");
        response.put("reply", reportDto);
        response.put("reports", reportDetailDto);

        return ResponseEntity.ok(response);
    }

    // 게시글 차단
    @ResponseBody
    @PostMapping("/posts/{postId}/block")
    public ResponseEntity<Map<String, String>> blockPost(@PathVariable Long postId,
                                                         @RequestParam Long reportId) {
        reportService.BlockPost(reportId, postId);
        return ResponseEntity.ok(Map.of("message", "게시글이 차단 되었습니다."));
    }

    // 게시글 차단 해제
    @ResponseBody
    @PostMapping("/posts/{postId}/unblock")
    public ResponseEntity<Map<String, String>> unblockPost(@PathVariable Long postId,
                                                           @RequestParam Long reportId) {
        reportService.unBlockPost(reportId, postId);
        return ResponseEntity.ok(Map.of("message", "게시글 차단이 해제 되었습니다."));
    }

    // 댓글 차단
    @ResponseBody
    @PostMapping("/replies/{replyId}/block")
    public ResponseEntity<Map<String, String>> blockReply(@PathVariable Long replyId,
                                                          @RequestParam Long reportId) {
        reportService.BlockReply(reportId, replyId);
        return ResponseEntity.ok(Map.of("message", "댓글이 차단 되었습니다."));
    }

    // 댓글 차단 해제
    @ResponseBody
    @PostMapping("/replies/{replyId}/unblock")
    public ResponseEntity<Map<String, String>> unblockReply(@PathVariable Long replyId,
                                                            @RequestParam Long reportId) {
        reportService.unBlockReply(reportId, replyId);
        return ResponseEntity.ok(Map.of("message", "댓글 차단이 해제 되었습니다."));
    }
}
