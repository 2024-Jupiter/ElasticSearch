package com.myfeed.controller;


import com.myfeed.annotation.CurrentUser;
import com.myfeed.log.annotation.LogUserBehavior;
import com.myfeed.model.elastic.PostEsClientDto;
import com.myfeed.model.elastic.SearchField;
import com.myfeed.model.elastic.UserSearchLogEs;
import com.myfeed.model.elastic.post.PostEs;
import com.myfeed.model.elastic.post.PostEsDetailDto;
import com.myfeed.model.elastic.post.PostEsListDto;
import com.myfeed.model.user.User;
import com.myfeed.service.Post.EsLogService;
import com.myfeed.service.Post.PostEsService;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/search/posts")
public class PostEsController {
    @Autowired PostEsService postEsService;
    @Autowired EsLogService esLogService;

    // 최신 게시글 조회 (비유저 메인 페이지 )
//    @GetMapping("/recent")
//    @ResponseBody
//    public Page<PostEs> getRecentPosts(
//            @RequestParam(name = "p",defaultValue = "1") int page
//    ) {
//        return postEsService.getRecentPosts(page);
//    }
//
    // 기본 검색(뉴스 제외) ( 제목, 내용, 제목+내용 )
    @GetMapping
//    @ResponseBody
    @LogUserBehavior
//    public Page<PostEs> searchPosts(
    public String searchPosts(

        @RequestParam String q,
        @RequestParam(name = "p", defaultValue = "1") int page,
        @RequestParam(name = "field", defaultValue = "TITLE") SearchField field,
            @CurrentUser User user, Model model
    ) throws IOException {
        // 검색 결과를 가져오기
        Page<PostEs> posts = postEsService.searchGeneralPosts(q, field, page, user);

        // 검색 결과를 DTO로 변환
        List<PostEsListDto> postList = posts.getContent().stream().map(post -> {
            return new PostEsListDto(
                    post.getId(),
                    post.getTitle(),
                    null,
                    post.getNickname(),
                    post.getCreatedAt()
            );
        }).collect(Collectors.toList());

        // 페이지네이션 구성
        int totalPages = posts.getTotalPages();
        int startPage = (int) Math.ceil((page - 0.5) / postEsService.PAGE_SIZE - 1) * postEsService.PAGE_SIZE + 1;
        int endPage = Math.min(startPage + postEsService.PAGE_SIZE - 1, totalPages);

        List<Integer> pageList = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageList.add(i);
        }

        // 모델에 데이터 추가
        model.addAttribute("postList", postList);
        model.addAttribute("query", q); // 검색어
        model.addAttribute("field", field); // 검색 필드
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageList", pageList);

        return "es/listEs";
//        return postEsService.searchGeneralPosts(q,field, page, user);
    }


    // 최신 게시글 조회 (비유저 메인 페이지 )
    @GetMapping("/recent")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRecentPosts(
            @RequestParam(name = "p", defaultValue = "1") int page
    ) {
        Page<PostEs> posts = postEsService.getRecentPosts(page);
        List<PostEsListDto> postList = posts.getContent().stream().map(postEs -> new PostEsListDto(
                postEs.getId(),
                postEs.getTitle(),
                null,
                postEs.getNickname(),
                postEs.getCreatedAt()
        )).toList();

        int totalPages = posts.getTotalPages();
        int startPage = (int) Math.ceil((page - 0.5) / postEsService.PAGE_SIZE - 1) * postEsService.PAGE_SIZE + 1;
        int endPage = Math.min(startPage + postEsService.PAGE_SIZE - 1, totalPages);
        List<Integer> pageList = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageList.add(i);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("postList", postList);
        response.put("totalPages", totalPages);
        response.put("startPage", startPage);
        response.put("endPage", endPage);
        response.put("pageList", pageList);

        return ResponseEntity.ok(response);
    }


    // ID로 postES 상세 검색
    @GetMapping("/{id}")
    public String getPostById(@PathVariable String id, Model model) {
        PostEs post = postEsService.findById(id);
//        PostEsDetailDto postDetailDto = PostEsDetailDto.builder()
//                .id(post.getId())
//                .title(post.getTitle())
//                .content(Jsoup.clean(post.getContent(), Safelist.basic()))
//                .nickname(post.getNickname())
//                .build();
        PostEsClientDto postEsClientDto = PostEsClientDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(Jsoup.clean(post.getContent(), Safelist.basic()))
                .nickname(post.getNickname())
                .viewCount(post.getViewCount())
                .createdAt(String.valueOf(post.getCreatedAt()))
                .build();

        model.addAttribute("post", postEsClientDto);
        return "es/detailEs";
    }


    // 나의 검색 로그 상위 3위 키워드로 게시글 추천
    // 1달간의 검색 로그 중 인기 검색어를 가져오는 쿼리를 활용한 추천
    @GetMapping("/recommend/by-my-top3-keywords")
    @ResponseBody
    public Page<PostEsClientDto> recommend(@RequestParam(name="p", defaultValue = "1") int page, HttpSession session, Model model,
            @CurrentUser User user)
            throws IOException {
        if (user == null) {
            return postEsService.getRecommendPostByTop3Keywords(page);
        }
        return postEsService.getRecommendPostForMe(page,user.getId().toString());
    }

    // 비슷한 게시물 추천
    @GetMapping("/recommend/{postId}/similar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRecommendationsByPostId(
            @PathVariable String postId,
            @PageableDefault(size = 10) Pageable pageable
    ) throws IOException {

        Page<PostEsClientDto> posts = postEsService.findSimilarPostsById(postId, pageable);

        Map<String, Object> response = new HashMap<>();

        List<PostEsListDto> postList = posts.getContent().stream().map(postEs -> new PostEsListDto(
                postEs.getId(),
                postEs.getTitle(),
                postEs.getContent(),
                null,
                null
        )).toList();

        response.put("similarPost", postList);

        return ResponseEntity.ok(response);

    }




    // 이하 안쓰는 코드

    // 비슷한 게시물 추천 - keywords로 검색 ( 사용 x 작동 o)
    @GetMapping("/recommend/keywords")
    @ResponseBody
    public Page<PostEsClientDto> getRecommendationsByKeywords(
            @RequestParam List<String> keywords,
            @PageableDefault(size = 10) Pageable pageable
    ) throws IOException {
        System.out.println("keywords: " + keywords);
        return postEsService.findSimilarPostsByKeywords(keywords, pageable);
    }


    // 유저의 한달 간 검색어 상위 3위 게시글 추천( 사용 x )
    @GetMapping("/recommend/by-top3-keywords/month")
    @ResponseBody
    public String recommendByTop3KeywordsMonth(@RequestParam(name="p", defaultValue = "1") int page, HttpSession session, Model model,@RequestParam(name = "userId") String userId)
            throws IOException {
        var pagedResult = postEsService.getRecommendedPostsByMonthSearchLog(page,userId);
//        System.out.println("pagedResult: " + pagedResult);
        return "api/search/posts/recommend";
    }

    @GetMapping("/init/velog")
    @ResponseBody
    public String ElasticsearchPostsInit() {
        postEsService.initVelogData();
        return "<h1>일래스틱 서치에 velog 데이터를 저장 했습니다.</h1>";
    }

}
