package com.myfeed.controller;

import com.myfeed.annotation.CurrentUser;
import com.myfeed.exception.ExpectedException;
import com.myfeed.model.post.*;
;
import com.myfeed.model.reply.Reply;
import com.myfeed.model.reply.ReplyDetailDto;
import com.myfeed.model.user.User;
import com.myfeed.response.ErrorCode;
import com.myfeed.service.Post.PostEsService;
import com.myfeed.service.Post.PostService;
import com.myfeed.service.reply.ReplyService;
import com.myfeed.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private PostEsService postEsService;
    @Autowired
    private UserService userService;
    @Autowired
    private ReplyService replyService;

    // 게시글 작성 폼 (GET 요청 으로 폼을 가져옴)
    @GetMapping("/create")
    public String createPostForm() {
        return "board/create";
    }

    // 게시글 작성
    @PostMapping("/create")
    public String createPost(@CurrentUser User user, @Valid PostDto postDto,
                             RedirectAttributes re) {
        Map<String, Object> response = new HashMap<>();
        Long id = postService.createPost(user.getId(), postDto);
        Post post = postService.findPostById(id);
        if (!post.getUser().equals(user)) {
            throw new ExpectedException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
        re.addAttribute("id",id);
        return "redirect:/api/posts/detail";
    }

    @GetMapping("/list")
    public String postList(@RequestParam(name = "p", defaultValue = "1") int page, Model model) {
        Page<Post> posts = postService.getPagedPosts(page);

        List<Long> userIds = posts.stream()
                .map(post -> post.getUser().getId())
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> userNicknames = userService.findUserNicknameByIds(userIds);

        List<PostListDto> postList = posts.getContent().stream().map(post -> {
            String authorName = userNicknames.getOrDefault(post.getUser().getId(), "알 수 없음");
            return new PostListDto(
                    post.getId(),
                    post.getTitle(),
                    null,
                    authorName,
                    post.getCreatedAt()
            );
        }).collect(Collectors.toList());

        int totalPages = posts.getTotalPages();
        int startPage =
                (int) Math.ceil((page - 0.5) / postService.PAGE_SIZE - 1) * postService.PAGE_SIZE
                        + 1;
        int endPage = Math.min(startPage + postService.PAGE_SIZE - 1, totalPages);
        List<Integer> pageList = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageList.add(i);
        }

        model.addAttribute("postList", postList);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageList", pageList);

        return "board/list";
    }

    // 게시글 상세 보기(댓글 페이지 네이션 & 조회수 증가 & 좋아요 기능)
    @GetMapping("/detail")
    public String detail(
            @RequestParam(name = "p", defaultValue = "1") int page, @RequestParam("id") long id,
            @RequestParam(name = "likeAction", required = false) String likeAction, Model model) {
        Post post = postService.findPostById(id);

        // 조회수 증가 (동시성)
        postService.incrementPostViewCountById(id);

        if ("like".equals(likeAction)) {
            // 좋아요 증가 (동시성)
            postService.incrementPostLikeCountById(id);
        } else {
            // 좋아요 감소 (동시성)
            postService.decrementPostLikeCountById(id);
        }

        PostDetailDto postDetailDto = new PostDetailDto(post);

        Page<Reply> replies = replyService.getPagedRepliesByPost(page, id);


        replies.getContent().forEach(reply -> {
            if (reply.getStatus() == BlockStatus.BLOCK_STATUS) {
                reply.setContent("차단된 댓글 입니다.");
            }
        });

        List<ReplyDetailDto> replyDetailDto = replies.getContent().stream()
                .map(ReplyDetailDto::new)
                .toList();
        System.out.println("list" + replyDetailDto);

        int totalPages = replies.getTotalPages();
        int startPage = (int) Math.ceil((page - 0.5) / postService.PAGE_SIZE - 1) * postService.PAGE_SIZE + 1;
        int endPage = Math.min(startPage + postService.PAGE_SIZE - 1, totalPages);
        List<Integer> pageList = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageList.add(i);
        }

        model.addAttribute("id", id);
        model.addAttribute("post", postDetailDto);
        model.addAttribute("replies", replyDetailDto);
        model.addAttribute("repliesCount", replyDetailDto.size());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageList", pageList);

        return "board/detail";
    }

    // 게시글 수정
    @PatchMapping("/{id}")
    public String updatePost(@PathVariable Long id, @CurrentUser User user, @Valid UpdateDto updateDto) {
        Post post = postService.findPostById(id);
        if (!post.getUser().equals(user)) {
            throw new ExpectedException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
        postService.updatePost(id, user, updateDto);

        return "redirect:/api/users/detail";
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public String deletePost(@PathVariable Long id, @CurrentUser User user) {
        Post post = postService.findPostById(id);
        if (!post.getUser().equals(user)) {
            throw new ExpectedException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
        postService.deletePostById(id, user);

        return "redirect:/api/users/detail";
    }

    // 게시글 조회수 증가
    @ResponseBody
    @PostMapping("/{id}/view")
    public ResponseEntity<Map<String, Object>> incrementPostViewCount(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            postService.incrementPostViewCountById(id);
            response.put("status", "success");
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("message", "게시글을 찾을 수 없습니다.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    // 게시글 좋아요 증가
    @ResponseBody
    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> incrementPostLikeCount(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            postService.incrementPostLikeCountById(id);
            response.put("status", "success");
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("message", "게시글을 찾을 수 없습니다.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    // 게시글 좋아요 감소
    @ResponseBody
    @PostMapping("/{id}/unlike")
    public ResponseEntity<Map<String, Object>> decrementPostLikeCount(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            postService.decrementPostLikeCountById(id);
            response.put("status", "success");
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("message", "게시글을 찾을 수 없습니다.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
}