package com.myfeed.controller;

import com.myfeed.model.post.BlockStatus;
import com.myfeed.model.post.Post;
import com.myfeed.model.reply.Reply;
import com.myfeed.model.reply.ReplyDetailDto;
import com.myfeed.model.reply.ReplyDto;
import com.myfeed.service.Post.PostService;
import com.myfeed.service.reply.ReplyService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/replies")
public class ReplyController {
    @Autowired
    private ReplyService replyService;
    @Autowired
    private PostService postService;

    // 댓글 작성 폼 (GET 요청 으로 폼을 가져옴)
    @GetMapping("/create")
    public String createReplyForm() {
        return "api/replies/create";
    }

    // 댓글 작성 (POST 요청)
    @ResponseBody
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createReply(@RequestParam Long userId,
                                                           @RequestParam Long postId,
                                                           @Valid @RequestBody ReplyDto replyDto) {
        Reply reply = replyService.createReply(userId, postId, replyDto);
        Map<String, Object> response = new HashMap<>();

        String redirectUrl = "/api/posts/detail/" + postId;
        response.put("redirectUrl",redirectUrl);
        response.put("success", true);
        response.put("message", "댓글이 작성 되었습니다.");
        response.put("data", reply);

        return ResponseEntity.ok(response);
    }

    // 게시글 내의 댓글 페이지 네이션 (동시성)
    @ResponseBody
    @GetMapping("/posts/detail/{postId}")
    public ResponseEntity<Map<String, Object>> getRepliesByPost(@PathVariable Long postId,
                                                                @RequestParam(name = "p", defaultValue = "1") int page,
                                                                HttpSession session) {
        Post post = postService.findPostById(postId);
        Page<Reply> replies = replyService.getPagedRepliesByPost(page, post);
        Map<String, Object> response = new HashMap<>();

        replies.getContent().forEach(reply -> {
            if (reply.getStatus() == BlockStatus.BLOCK_STATUS) {
                reply.setContent("차단된 댓글 입니다.");
            }
        });

        replies.getContent().forEach(reply -> {
            if (reply.getStatus() == BlockStatus.BLOCK_STATUS) {
                reply.setContent("차단된 댓글 입니다.");
            }
        });

        List<ReplyDetailDto> replyDetailDto = replies.getContent().stream()
                .map(ReplyDetailDto::new)
                .toList();

        int totalPages = replies.getTotalPages();
        int startPage = (int) Math.ceil((page - 0.5) / replyService.PAGE_SIZE - 1) * replyService.PAGE_SIZE + 1;
        int endPage = Math.min(startPage + replyService.PAGE_SIZE - 1, totalPages);
        List<Integer> pageList = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageList.add(i);
        }

        session.setAttribute("currentPostPage", page);
        String redirectUrl = "/api/posts/detail/" + post.getId();
        response.put("redirectUrl",redirectUrl);
        response.put("success", true);
        response.put("message", "게시글 내의 댓글들");
        response.put("data", replyDetailDto);
        response.put("totalPages", totalPages);
        response.put("startPage", startPage);
        response.put("endPage", endPage);
        response.put("pageList", pageList);

        return ResponseEntity.ok(response);
    }

    // 댓글 수정
    @ResponseBody
    @PatchMapping("/{id}")
    @PreAuthorize("#user.id == authentication.principal.id")
    public ResponseEntity<Map<String, Object>> updateReply(@PathVariable Long id,
                                                           @Valid @RequestBody ReplyDto replyDto) {
        Reply reply = replyService.findByReplyId(id);
        replyService.updateReply(id, replyDto);
        Map<String, Object> response = new HashMap<>();

        String redirectUrl = "/api/posts/detail/" + reply.getPost().getId();
        response.put("redirectUrl", redirectUrl);
        response.put("success", true);
        response.put("message", "댓글이 수정 되었습니다.");
        response.put("data", reply);

        return ResponseEntity.ok(response);
    }

    // 댓글 삭제
    @ResponseBody
    @DeleteMapping("/{id}")
    @PreAuthorize("#user.id == authentication.principal.id")
    public ResponseEntity<Map<String, Object>> deleteReply(@PathVariable Long id) {
        Reply reply = replyService.findByReplyId(id);
        replyService.deleteReply(id);
        Map<String, Object> response = new HashMap<>();

        String redirectUrl = "/api/posts/detail/" + reply.getPost().getId();
        response.put("redirectUrl", redirectUrl);
        response.put("success", true);
        response.put("message", "댓글이 삭제 되었습니다.");
        response.put("data", reply);

        return ResponseEntity.ok(response);
    }
}

