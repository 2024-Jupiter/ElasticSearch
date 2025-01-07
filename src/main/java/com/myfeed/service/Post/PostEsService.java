package com.myfeed.service.Post;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.MoreLikeThisQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.myfeed.model.elastic.PostEsDto1;
import com.myfeed.model.elastic.SearchField;
import com.myfeed.model.elastic.post.PostEs;
import com.myfeed.model.elastic.post.ReplyEs;
import com.myfeed.repository.elasticsearch.PostEsDataRepository;
import com.myfeed.repository.elasticsearch.PostEsRepository;
import com.myfeed.service.Post.crawlingdata.NewsJsonReader;
import com.myfeed.service.Post.record.KeywordCount;
import com.myfeed.service.Post.record.NewsDto;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import com.myfeed.model.post.*;
import com.myfeed.service.Post.crawlingdata.VelogDto;
import com.myfeed.service.Post.crawlingdata.VelogJsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostEsService {
    public static final int PAGE_SIZE = 10;
    @Autowired private EsLogService esLogService;
    @Autowired private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired private PostEsDataRepository postEsDataRepository;
    @Autowired private PostEsRepository postEsRepository;
    @Autowired private ElasticsearchClient elasticsearchClient;

    @Autowired private ElasticsearchOperations elasticsearchOperations;

    // 제목 검색 - 일반 게시글
    public Page<PostEs> searchGeneralPosts(String keyword, SearchField field,int page) {
        // 결과 값 선언
        Page<PostEs> posts = null;
        PageRequest pageRequest = PageRequest.of(page - 1, 10);
        posts = switch (field) {
            // 제목 검색
            case TITLE -> postEsRepository.searchGeneralPostsByTitle(keyword, pageRequest);

            // 내용 검색
            case CONTENT -> postEsRepository.searchGeneralPostsByContent(keyword, pageRequest);

            // 제목+내용 검색
            case TITLE_CONTENT -> postEsRepository.searchGeneralPostsByTitleAndContent(keyword, pageRequest);
        };
        System.out.println("posts: " + posts.getContent().get(0).getTitle());
        return posts;
    }


    // 생성, 수정, 조회수 & 좋아요 (비동기 동기화)
    @Async
    public void syncToElasticsearch(PostEs postEs) {
        postEsDataRepository.save(postEs);
    }

    // 삭제
    @Async
    public void deleteFromElasticsearch(String id) {
        postEsDataRepository.deleteById(id);
        System.out.println("Deleted from Elasticsearch: ID=" + id);
    }


    public List<Map<String, Object>> getRecommendedPostsBySearchLog(int page,String userId) throws IOException {
        // 현재 시점에서 1달 전 날짜 계산
        String oneMonthAgo = ZonedDateTime.now().minusMonths(1).toString();
        // 검색 요청 생성
        SearchRequest searchRequest = SearchRequest.of(builder -> builder
            .index("user_search_logs")
            // 날짜 범위와 userId로 필터링
            .query(Query.of(q -> q
                .bool(b -> b
                    .must(List.of(
                        Query.of(must -> must
                            .term(term -> term
                                .field("userId")
                                .value(userId)
                            )
                        ),
                        Query.of(must -> must
                            .range(r -> r
                                .date(d -> d
                                    .field("timestamp")
                                    .gte(oneMonthAgo)
                                )
                            )
                        )
                    ))
                )
            ))
            .aggregations("keyword_counts", Aggregation.of(a -> a
                .terms(t -> t
                    .field("keyword")
                    .size(3) // top 3 결과만 가져옴
                )
            ))
        );
        // 검색 실행
        SearchResponse<Void> response = elasticsearchClient.search(searchRequest, Void.class);

        // 집계 결과 파싱 및 반환
        return response.aggregations()
            .get("keyword_counts")
            .sterms()
            .buckets().array()
            .stream()
            .map(bucket -> Map.of(
                "keyword", bucket.key(),
                "count", bucket.docCount()
            ))
            .toList();
    }
    // 특정 사용자의 검색로그 기반으로 추천하는 게시글
    public Page<PostEsDto1> getRecommendPostForMe(int page,String userId) throws IOException {
        List<KeywordCount> myAllTimeTopKeywords = esLogService.findMyAllTimeTopKeywords(userId);
        List<String> list = myAllTimeTopKeywords.stream().map(KeywordCount::keyword).toList();
        System.out.println("추천 검색어 " + list);
        return this.getPostBySearchLogs(page, list);
    }
    // 현재 사용자 검색 로그 상위 3개 키워드로 게시글 검색
    public Page<PostEsDto1> getRecommendPostByTop3Keywords(int page) throws IOException {
        List<KeywordCount> myAllTimeTopKeywords = esLogService.findAllTimeTopKeywords();
        List<String> list = myAllTimeTopKeywords.stream().map(KeywordCount::keyword).toList();
        System.out.println("추천 검색어2 " + list);
        return this.getPostBySearchLogs(page, list);
    }

    public Page<PostEsDto1> getPostBySearchLogs(int page, List<String> keywords) throws IOException {
        // 페이지네이션
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
        // 검색어
        String keyword1 = keywords.get(0);
        String keyword2 = keywords.get(1);
        String keyword3 = keywords.get(2);

        SearchRequest searchRequest = SearchRequest.of(builder -> builder
            .index("posts") // 인덱스 이름을 실제 사용하는 이름으로 변경해주세요
            .query(Query.of(q -> q
                .bool(b -> b
                    .should(List.of(
                        // keyword1 matches with boost 3
                        Query.of(s -> s
                            .match(m -> m
                                .field("title")
                                .query(keyword1)
                                .boost(3.0f)
                            )
                        ),
                        Query.of(s -> s
                            .match(m -> m
                                .field("content")
                                .query(keyword1)
                                .boost(3.0f)
                            )
                        ),
                        // keyword2 matches with boost 2
                        Query.of(s -> s
                            .match(m -> m
                                .field("title")
                                .query(keyword2)
                                .boost(2.0f)
                            )
                        ),
                        Query.of(s -> s
                            .match(m -> m
                                .field("content")
                                .query(keyword2)
                                .boost(2.0f)
                            )
                        ),
                        // keyword3 matches with boost 1
                        Query.of(s -> s
                            .match(m -> m
                                .field("title")
                                .query(keyword3)
                                .boost(1.0f)
                            )
                        ),
                        Query.of(s -> s
                            .match(m -> m
                                .field("content")
                                .query(keyword3)
                                .boost(1.0f)
                            )
                        )
                    ))
                )
            ))
            .from(pageable.getPageNumber() * pageable.getPageSize())
            .size(pageable.getPageSize())
        );



        SearchResponse<PostEsDto1> response = elasticsearchClient.search(searchRequest, PostEsDto1.class);

        List<PostEsDto1> posts = response.hits().hits().stream()
            .map(Hit::source)
            .toList();
        System.out.println("posts: " + posts.get(0).getTitle());
        return new PageImpl<>(
            posts,
            pageable,
            response.hits().total().value()
        );
    }

    // 1. 키워드 기반 추천 (Match 쿼리 사용)
    public Page<PostEsDto1> findSimilarPostsByKeywords(List<String> keywords, Pageable pageable) throws IOException {
        // 키워드들을 하나의 문자열로 합침
        String combinedKeywords = String.join(" ", keywords);

        SearchRequest searchRequest = SearchRequest.of(builder -> builder
            .index("posts")
            .query(Query.of(q -> q
                .bool(b -> b
                    .must(m -> m
                        .multiMatch(mm -> mm
                            .query(combinedKeywords)
                            .fields(Arrays.asList("title^2", "content^1"))
                            .minimumShouldMatch("30%")  // 최소 30% 매칭되어야 결과에 포함
                            .tieBreaker(0.3)  // 여러 필드 매칭시 스코어 계산에 사용
                        )
                    )
                )
            ))
            .from(pageable.getPageNumber() * pageable.getPageSize())
            .size(pageable.getPageSize())
        );

        SearchResponse<PostEsDto1> response = elasticsearchClient.search(searchRequest, PostEsDto1.class);

        List<PostEsDto1> posts = response.hits().hits().stream()
            .map(hit -> hit.source())
            .toList();
        System.out.println("posts: " + posts.get(0).getTitle());
        System.out.println("posts: " + posts.get(0).getContent());
        return new PageImpl<>(posts, pageable, response.hits().total().value());
    }

    // 2. 문서 기반 추천 (More Like This 사용)
    public Page<PostEsDto1> findSimilarPostsById(String postId, Pageable pageable) throws IOException {
        System.out.println("postId: " + postId);
        SearchRequest searchRequest = SearchRequest.of(builder -> builder
            .index("posts")
            .query(Query.of(q -> q
                .moreLikeThis(MoreLikeThisQuery.of(mlt -> mlt
                    .fields("title", "content")  // 유사도를 비교할 필드들
                    .like(l -> l
                        .document(d -> d
                            .index("posts")
                            .id(postId)
                        )
                    )
                    .minTermFreq(1)        // 원본 문서에서 최소 등장 횟수
                    .maxQueryTerms(12)      // 쿼리에 사용할 최대 텀 수
                    .minDocFreq(1)         // 최소 문서 빈도
                    .minimumShouldMatch("30%")  // 최소 매칭 비율
                    .boostTerms(0.5)      // 텀 빈도수에 따른 부스트
                ))
            ))
            .from(pageable.getPageNumber() * pageable.getPageSize())
            .size(pageable.getPageSize())
        );

        SearchResponse<PostEsDto1> response = elasticsearchClient.search(searchRequest, PostEsDto1.class);

        List<PostEsDto1> posts = response.hits().hits().stream()
            .map(Hit::source)
            .toList();
        if (posts == null) {
            System.out.println("posts is null");
            return null;
        }
        return new PageImpl<>(posts, pageable, response.hits().total().value());
    }

    public Page<PostEsDto1> searchGeneralPosts(String keyword, Pageable pageable) {
        postEsDataRepository.findAll(pageable);
        return null;
    }

    @Async
    public void initVelogData() {
        System.out.println("Inserting by JSON file...");
        List<VelogDto> velogDtos = new VelogJsonReader().loadJson();

        List<PostEs> list = velogDtos.stream().map(velogDto -> {
            return PostEs.builder()
                    .nickname(velogDto.getUserName())
                    .title(velogDto.getTitle())
                    .content(velogDto.getContent())
                    .category(Category.GENERAL)
                    .viewCount(0)
                    .likeCount(velogDto.getLikeCount())
                    .replies(velogDto.velogCommentToReplyEs())
                    .createdAt(LocalDateTime.parse(velogDto.getDate()))
                    .build();
        }).toList();
        postEsDataRepository.saveAll(list);
        System.out.println("Successfully saved to Elasticsearch");
    }

    @Async
    public void initNewsData() {
        System.out.println("Inserting news data...");
        List<NewsDto> newsDtos = new NewsJsonReader().loadJson();
        postEsDataRepository.saveAll(newsDtos.stream().map(newsDto -> {
            return PostEs.builder()
                    .nickname(newsDto.author())
                    .title(newsDto.title())
                    .content(newsDto.content())
                    .category(Category.NEWS)
                    .viewCount(0)
                    .likeCount(0)
                    .replies(new ArrayList<ReplyEs>())
                    .createdAt(newsDto.getParsedDate())
                    .build();
        }).toList());
    }

}
