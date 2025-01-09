package com.myfeed.controller;


import com.myfeed.log.annotation.LogUserBehavior;
import com.myfeed.model.elastic.PostEsClientDto;
import com.myfeed.model.elastic.SearchField;
import com.myfeed.model.elastic.UserSearchLogEs;
import com.myfeed.model.elastic.post.PostEs;
import com.myfeed.service.Post.EsLogService;
import com.myfeed.service.Post.PostEsService;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    @GetMapping("/recent")
    @ResponseBody
    public Page<PostEs> getRecentPosts(
            @RequestParam(name = "p",defaultValue = "1") int page
    ) {
        return postEsService.getRecentPosts(page);
    }

    // 기본 검색(뉴스 제외) ( 제목, 내용, 제목+내용 )
    @GetMapping
    @ResponseBody
    @LogUserBehavior
    public Page<PostEs> searchPosts(
        @RequestParam String q,
        @RequestParam(name = "p", defaultValue = "1") int page,
        @RequestParam(name = "field", defaultValue = "TITLE") SearchField field
    ) throws IOException {
        return postEsService.searchGeneralPosts(q,field, page);
    }

    // ID로 postES 상세 검색
    @GetMapping("/{id}")
    @ResponseBody
    public PostEs getPostById(@PathVariable String id) {
        return postEsService.findById(id);
    }

    // 사용자 검색 로그 상위 3위 키워드로 게시글 추천
    @GetMapping("/recommend/by-top3-keywords")
    @ResponseBody
    public Page<PostEsClientDto> recommendByTop3Keywords(@RequestParam(name="p", defaultValue = "1") int page, HttpSession session, Model model)
        throws IOException {
        return postEsService.getRecommendPostByTop3Keywords(page);
    }
    // 나의 검색 로그 상위 3위 키워드로 게시글 추천
    @GetMapping("/recommend/by-my-top3-keywords")
    @ResponseBody
    public String recommend(@RequestParam(name="p", defaultValue = "1") int page, HttpSession session, Model model,@RequestParam(name = "userId") String userId)
            throws IOException {
        var pagedResult = postEsService.getRecommendPostForMe(page,userId);
        return "api/search/posts/recommend";
    }
    // 유저의 한달 간 검색어 상위 3위 게시글 추천( 사용 x )
    @GetMapping("/recommend/by-top3-keywords/month")
    @ResponseBody
    public String recommendByTop3KeywordsMonth(@RequestParam(name="p", defaultValue = "1") int page, HttpSession session, Model model,@RequestParam(name = "userId") String userId)
        throws IOException {
        var pagedResult = postEsService.getRecommendedPostsByMonthSearchLog(page,userId);
        System.out.println("pagedResult: " + pagedResult);
        return "api/search/posts/recommend";
    }

    // 비슷한 게시물 추천
    @GetMapping("/recommend/{postId}/similar")
    @ResponseBody
    public Page<PostEsClientDto> getRecommendationsByPostId(
        @PathVariable String postId,
        @PageableDefault(size = 10) Pageable pageable
    ) throws IOException {
        return postEsService.findSimilarPostsById(postId, pageable);
    }
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


    @GetMapping("/init/velog")
    @ResponseBody
    public String ElasticsearchPostsInit() {
        postEsService.initVelogData();
        return "<h1>일래스틱 서치에 velog 데이터를 저장 했습니다.</h1>";
    }

}
