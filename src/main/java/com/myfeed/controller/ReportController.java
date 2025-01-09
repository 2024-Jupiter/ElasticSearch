package com.myfeed.controller;

import com.myfeed.exception.ExpectedException;
import com.myfeed.model.post.BlockStatus;
import com.myfeed.model.post.Post;
import com.myfeed.model.post.PostListDto;
import com.myfeed.model.post.PostReportDto;
import com.myfeed.model.reply.Reply;
import com.myfeed.model.reply.ReplyListDto;
import com.myfeed.model.reply.ReplyReportDto;
import com.myfeed.model.report.*;
import com.myfeed.response.ErrorCode;
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
import org.springframework.ui.Model;
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
    @GetMapping("/posts/form")
    public String reportPostForm() {
        return "board/create";
        // return "report/post";
    }

    // 게시글 신고
    @PostMapping("/posts/{postId}")
    public String reportPost(@PathVariable Long postId, @Valid ReportDto reportDto) {
        reportService.reportPost(postId, reportDto);

        return "board/test";
    }

    // 댓글 신고 폼 (GET 요청 으로 폼을 가져옴)
    @GetMapping("/replies/form")
    public String reportReplyForm() {
        return "board/create";
        // return "report/replies";
    }

    // 댓글 신고
    @PostMapping("/replies/{replyId}")
    public String reportReply(@PathVariable Long replyId, @Valid ReportDto reportDto) {
        reportService.reportReply(replyId, reportDto);

        return "board/test";
    }

    // 신고된 게시글 페이지 네이션 (동시성)
    @GetMapping("/posts/list")
    public String getPostsByReport(@RequestParam(name="p", defaultValue = "1") int page,
                                   @Valid @RequestParam(name="status", required = false) String status,
                                   Model model) {
        Page<Post> posts = reportService.getReportedPosts(page);
        List<PostListDto> postList = posts.getContent().stream().map(post -> {
            return new PostListDto(
                    post.getId(),
                    post.getTitle(),
                    null,
                    post.getUser().getNickname(),
                    post.getCreatedAt()
            );
        }).toList();

        // 차단 상태에 따라 다르게 보이게
        if (status != null) {
            BlockStatus blockStatus = BlockStatus.valueOf(status.toUpperCase());
            List<Post> filteredRPosts;

            if (blockStatus == BlockStatus.NORMAL_STATUS) {
                filteredRPosts = posts.stream()
                        .filter(post -> post.getStatus() == BlockStatus.NORMAL_STATUS)
                        .collect(Collectors.toList());
            } else if (blockStatus == BlockStatus.BLOCK_STATUS) {
                filteredRPosts = posts.stream()
                        .filter(post -> post.getStatus() == BlockStatus.BLOCK_STATUS)
                        .collect(Collectors.toList());
            } else {
                throw new ExpectedException(ErrorCode.NOT_REPORTED);
            }

            posts = new PageImpl<>(filteredRPosts, posts.getPageable(), filteredRPosts.size());
        }

        int totalPages = posts.getTotalPages();
        int startPage = (int) Math.ceil((page - 0.5) / postService.PAGE_SIZE - 1) * postService.PAGE_SIZE + 1;
        int endPage = Math.min(startPage + postService.PAGE_SIZE - 1, totalPages);
        List<Integer> pageList = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageList.add(i);
        }

        model.addAttribute("postList", postList);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageList", pageList);

        return "board/test";
        //return "report/post/list";
    }

    // 신고된 댓글 페이지 네이션 (동시성)
    @GetMapping("/replies/list")
    public String getRepliesByReport(@RequestParam(name="p", defaultValue = "1") int page,
                                   @Valid @RequestParam(name="status", required = false) String status,
                                   Model model) {
        Page<Reply> replies = reportService.getReportedReplies(page);
        List<ReplyListDto> replyList = replies.getContent().stream().map(reply -> {
            return new ReplyListDto(
                    reply.getId(),
                    reply.getContent(),
                    reply.getUser().getNickname(),
                    reply.getCreatedAt()
            );
        }).toList();

        // 차단 상태에 따라 다르게 보이게
        if (status != null) {
            BlockStatus blockStatus = BlockStatus.valueOf(status.toUpperCase());
            List<Reply> filteredRReplies;

            if (blockStatus == BlockStatus.NORMAL_STATUS) {
                filteredRReplies = replies.stream()
                        .filter(reply -> reply.getStatus() == BlockStatus.NORMAL_STATUS)
                        .collect(Collectors.toList());
            } else if (blockStatus == BlockStatus.BLOCK_STATUS) {
                filteredRReplies = replies.stream()
                        .filter(reply -> reply.getStatus() == BlockStatus.BLOCK_STATUS)
                        .collect(Collectors.toList());
            } else {
                throw new ExpectedException(ErrorCode.NOT_REPORTED);
            }

            replies = new PageImpl<>(filteredRReplies, replies.getPageable(), filteredRReplies.size());
        }

        int totalPages = replies.getTotalPages();
        int startPage = (int) Math.ceil((page - 0.5) / postService.PAGE_SIZE - 1) * postService.PAGE_SIZE + 1;
        int endPage = Math.min(startPage + postService.PAGE_SIZE - 1, totalPages);
        List<Integer> pageList = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageList.add(i);
        }

        model.addAttribute("replyList", replyList);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageList", pageList);

        return "board/list";
        //return "report/reply/list";
    }

    // 신고 게시글 리스트 페이지 네이션 (동시성)
    @GetMapping("/posts/{postId}/list")
    public String getReportsByPost(@PathVariable Long postId, @RequestParam(name="p", defaultValue = "1") int page,
                                   @Valid @RequestParam(name="status", required = false) String status,
                                   Model model) {
        Post post = postService.findPostById(postId);
        PostReportDto postReportDto = new PostReportDto(post);

        Page<Report> reports = reportService.getPagedReportsByPost(page, postId);
        // 게시글 신고 리스트
        List<ReportListDto> reportList = reports.stream()
                .map(ReportListDto::new)
                .toList();

        // 처리 상태에 따라 다르게 보이게
        if (status != null) {
            ProcessStatus processStatus = ProcessStatus.valueOf(status.toUpperCase());
            List<Report> filteredReports;

            if (processStatus == ProcessStatus.PENDING) {
                filteredReports = reports.stream()
                        .filter(report -> report.getStatus() == ProcessStatus.PENDING)
                        .collect(Collectors.toList());
            } else if (processStatus == ProcessStatus.COMPLETED) {
                filteredReports = reports.stream()
                        .filter(report -> report.getStatus() == ProcessStatus.COMPLETED)
                        .collect(Collectors.toList());
            } else {
                throw new ExpectedException(ErrorCode.NOT_REPORTED);
            }

            reports = new PageImpl<>(filteredReports, reports.getPageable(), filteredReports.size());
        }

        int totalPages = reports.getTotalPages();
        int startPage = (int) Math.ceil((page - 0.5) / postService.PAGE_SIZE - 1) * postService.PAGE_SIZE + 1;
        int endPage = Math.min(startPage + postService.PAGE_SIZE - 1, totalPages);
        List<Integer> pageList = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageList.add(i);
        }

        model.addAttribute("post", postReportDto);
        model.addAttribute("reportList", reportList);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageList", pageList);

        return "board/test";
        //return "post/report/list";
    }

    // 신고 댓글 리스트 페이지 네이션 (동시성)
    @GetMapping("/replies/{replyId}/list")
    public String getReportsByReply(@PathVariable Long replyId, @RequestParam(name="p", defaultValue = "1") int page,
                                    @Valid @RequestParam(name="status", required = false) String status,
                                    Model model) {
        Reply reply = replyService.findByReplyId(replyId);
        ReplyReportDto replyReportDto = new ReplyReportDto(reply);

        Page<Report> reports = reportService.getPagedReportsByReply(page, replyId);
        // 게시글 신고 리스트
        List<ReportListDto> reportList = reports.stream()
                .map(ReportListDto::new)
                .toList();

        // 처리 상태에 따라 다르게 보이게
        if (status != null) {
            ProcessStatus processStatus = ProcessStatus.valueOf(status.toUpperCase());
            List<Report> filteredReports;

            if (processStatus == ProcessStatus.PENDING) {
                filteredReports = reports.stream()
                        .filter(report -> report.getStatus() == ProcessStatus.PENDING)
                        .collect(Collectors.toList());
            } else if (processStatus == ProcessStatus.COMPLETED) {
                filteredReports = reports.stream()
                        .filter(report -> report.getStatus() == ProcessStatus.COMPLETED)
                        .collect(Collectors.toList());
            } else {
                throw new ExpectedException(ErrorCode.NOT_REPORTED);
            }

            reports = new PageImpl<>(filteredReports, reports.getPageable(), filteredReports.size());
        }

        int totalPages = reports.getTotalPages();
        int startPage = (int) Math.ceil((page - 0.5) / replyService.PAGE_SIZE - 1) * replyService.PAGE_SIZE + 1;
        int endPage = Math.min(startPage + replyService.PAGE_SIZE - 1, totalPages);
        List<Integer> pageList = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageList.add(i);
        }

        model.addAttribute("reply", replyReportDto);
        model.addAttribute("reportList", reportList);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageList", pageList);

        return "board/test";
        //return "reply/report/list";
    }

    // 신고 내역 상세 보기
    @GetMapping("/{id}/detail")
    public String reportDetail(@PathVariable Long id, Model model) {
        Report report = reportService.findByReportId(id);
        ReportDetailDto reportDetailDto = new ReportDetailDto(report);

        model.addAttribute("reportDetail", reportDetailDto);

        return "board/test";
    }

    // 게시글 차단
    @ResponseBody
    @PostMapping("/posts/block")
    public ResponseEntity<Map<String, String>> blockPost(@RequestParam Long id) {
        reportService.BlockPost(id);
        return ResponseEntity.ok(Map.of("message", "게시글이 차단 되었습니다."));
    }

    // 게시글 차단 해제
    @ResponseBody
    @PostMapping("/posts/unblock")
    public ResponseEntity<Map<String, String>> unblockPost(@RequestParam Long id) {
        reportService.unBlockPost(id);
        return ResponseEntity.ok(Map.of("message", "게시글 차단이 해제 되었습니다."));
    }

    // 댓글 차단
    @ResponseBody
    @PostMapping("/replies/block")
    public ResponseEntity<Map<String, String>> blockReply(@RequestParam Long id) {
        reportService.BlockReply(id);
        return ResponseEntity.ok(Map.of("message", "댓글이 차단 되었습니다."));
    }

    // 댓글 차단 해제
    @ResponseBody
    @PostMapping("/replies/unblock")
    public ResponseEntity<Map<String, String>> unblockReply(@RequestParam Long id) {
        reportService.unBlockReply(id);
        return ResponseEntity.ok(Map.of("message", "댓글 차단이 해제 되었습니다."));
    }
}
