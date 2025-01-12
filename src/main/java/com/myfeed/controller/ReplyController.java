package com.myfeed.controller;

import com.myfeed.annotation.CurrentUser;
import com.myfeed.exception.ExpectedException;
import com.myfeed.model.post.BlockStatus;
import com.myfeed.model.post.Post;
import com.myfeed.model.reply.Reply;
import com.myfeed.model.reply.ReplyDetailDto;
import com.myfeed.model.reply.ReplyDto;
import com.myfeed.model.user.User;
import com.myfeed.response.ErrorCode;
import com.myfeed.service.Post.PostService;
import com.myfeed.service.reply.ReplyService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/replies")
public class ReplyController {
    @Autowired private ReplyService replyService;
    @Autowired private PostService postService;

    // 댓글 작성 폼 (GET 요청 으로 폼을 가져옴)
    @GetMapping("/create")
    public String createReplyForm() {
        return "board/create";
    }

    // 댓글 작성 (POST 요청)
    @PostMapping("/create")
    public String createReply(@CurrentUser User user, @RequestParam Long postId,
                              @Valid ReplyDto replyDto, RedirectAttributes re) {
        System.out.println("댓글" + postId);
        Map<String, Object> response = new HashMap<>();
        Long id = replyService.createReply(user.getId(), postId, replyDto);
        Reply reply = replyService.findByReplyId(id);
//        if (!reply.getUser().equals(user)) {
//            throw new ExpectedException(ErrorCode.AUTHENTICATION_REQUIRED);
//        }
        re.addAttribute("id",postId);
        String redirectUrl = "/api/posts/detail/" + id;
        response.put("redirectUrl", redirectUrl);
        response.put("success", true);
        response.put("message", "댓글이 작성 되었습니다.");

        return "redirect:/api/posts/detail";
    }

    // 게시글 내의 댓글 페이지 네이션 (동시성)
    @GetMapping("/posts/detail/{postId}")
    public String getRepliesByPost(@PathVariable Long postId,
                                   @RequestParam(name = "p", defaultValue = "1") int page,
                                   Model model) {
        Page<Reply> replies = replyService.getPagedRepliesByPost(page, postId);

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

        model.addAttribute("replies", replyDetailDto);
        model.addAttribute("repliesCount", replyDetailDto.size());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageList", pageList);

        return "board/test";
    }

    // 댓글 수정
    @PatchMapping("/{id}")
    public String updateReply(@PathVariable Long id, @CurrentUser User user,
                              @Valid ReplyDto replyDto) {
        Reply reply = replyService.findByReplyId(id);
        if (!reply.getUser().equals(user)) {
            throw new ExpectedException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
        replyService.updateReply(id, user, replyDto);

        return "board/test";
    }

    // 댓글 삭제
    @DeleteMapping("/{id}")
    public String deleteReply(@PathVariable Long id, @CurrentUser User user) {
        Reply reply = replyService.findByReplyId(id);
        if (!reply.getUser().equals(user)) {
            throw new ExpectedException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
        replyService.deleteReply(id, user);

        return "board/test";
    }
}