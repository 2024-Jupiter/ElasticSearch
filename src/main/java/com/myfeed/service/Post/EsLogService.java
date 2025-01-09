package com.myfeed.service.Post;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.myfeed.model.elastic.UserSearchLogEs;
import com.myfeed.repository.elasticsearch.SearchLogEsDataRepository;
import com.myfeed.service.Post.record.KeywordCount;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;

@Service
public class EsLogService {

    @Autowired private SearchLogEsDataRepository searchLogEsDataRepository;
    @Autowired private ElasticsearchClient esClient;
    @Autowired private ElasticsearchOperations esOperations;
    @Autowired private ElasticsearchTemplate elasticsearchTemplate;

    // 검색 로그 저장
    public void saveWithEsDataRepository(String userId, String searchWord) {
        System.out.println("===========saveSearchLog with repository==========");
        String uid = userId;
        if (userId == null || userId.isEmpty()) {
            uid = "anonymous";
        }
        UserSearchLogEs searchLog = UserSearchLogEs.builder()
            .id(UUID.randomUUID().toString())
            .userId(uid)
            .searchText(searchWord)
            .createdAt(LocalDateTime.now().withNano(0))
            .build();
        searchLogEsDataRepository.save(searchLog);
    }
    public void saveWithNativeQuery(String userId, String searchWord) {
        System.out.println("===========saveSearchLog with client==========");
        String uid = userId;
        if (userId == null || userId.isEmpty()) {
            uid = "anonymous";
        }
        UserSearchLogEs searchLog = UserSearchLogEs.builder()
            .id(UUID.randomUUID().toString())
            .userId(uid)
            .searchText(searchWord)
            .createdAt(LocalDateTime.now().withNano(0))
            .build();
        try {
            esClient.index(i -> i
                .index("user_search_logs")
                .document(searchLog)
            );
            System.out.println("==save done==");
        } catch (ElasticsearchException e) {
            System.out.println("====== 여기가 에러나나 error: " + e);
            // 에러 처리
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // decaying function을 이용한 검색 로그 조회 3순위까지
    public void getPopularSearchLogs() {
        System.out.println("===========getPopularSearchLogs==========");
        var query = NativeQuery.builder()
            .withQuery(new StringQuery("""
        {
          "function_score": {
            "query": {
              "match_all": {}
            },
            "functions": [
              {
                "exp": {
                  "createdAt": {
                    "scale": "7d",
                    "offset": "1d",
                    "decay": 0.5
                  }
                },
                "weight": 5.0
              }
            ],
            "boost_mode": "multiply"
          }
        }
        """))
            .withAggregation("top_keywords",
                Aggregation.of(a -> a
                    .terms(TermsAggregation.of(t -> t
                        .field("searchText.keyword")
                        .size(3)
                    ))
                )
            )
            .build();
        System.out.println("Native search :" + query);
        SearchHits<UserSearchLogEs> searchHits = elasticsearchTemplate.search(query, UserSearchLogEs.class);
        System.out.println("searchHits: " + searchHits);
//        searchHits.getAggregations().aggregations().;

    }
    // decaying function을 이용한 검색 로그 조회 3순위까지
    // 범위는 최근 30일 이내,
    public void getPopularKeyword() {
        var query = NativeQuery.builder()
        .withQuery(new StringQuery("""
        {
          "function_score": {
            "query": {
              "bool": {
                "filter": [
                  {
                    "range": {
                      "createdAt": {
                        "gte": "now-30d/d"
                      }
                    }
                  }
                ]
              }
            },
            "functions": [
              {
                "exp": {
                  "createdAt": {
                    "scale": "7d",
                    "offset": "1d",
                    "decay": 0.5
                  }
                },
                "weight": 5.0
              },
              {
                "field_value_factor": {
                  "field": "searchCount",
                  "factor": 1.2,
                  "modifier": "log1p",
                  "missing": 1
                }
              }
            ],
            "score_mode": "multiply",
            "boost_mode": "sum"
          }
        }
        """))
        .withAggregation("top_keywords",
                Aggregation.of(a -> a
                        .terms(t -> t
                                .field("searchText.keyword")
                                .size(3)
                        ))
        )
        .build();

        SearchHits<UserSearchLogEs> searchHits = elasticsearchTemplate.search(query, UserSearchLogEs.class);
        System.out.println("searchHits: " + searchHits.hasAggregations());
        var aggregations = searchHits.getAggregations();
        System.out.println("aggregations: " + aggregations.aggregations());
    }


    // 1. 전체 기간 상위 검색어
    public List<KeywordCount> findAllTimeTopKeywords() throws IOException {
        System.out.println("===========findAllTimeTopKeywords==========");
        SearchRequest request = SearchRequest.of(builder -> builder
            .index("user_search_logs")
            .size(0)
            .aggregations("top_keywords",
                Aggregation.of(a -> a
                    .terms(TermsAggregation.of(t -> t
                        .field("searchText")
                        .size(3)
                    ))
                )
            )
        );
        SearchResponse<Void> response = esClient.search(request, Void.class);
        return parseKeywordAggregation(response, "top_keywords");
    }
    // 1. 내가 검색한 모든 시간 상위 검색어 3개
    public List<KeywordCount> findMyAllTimeTopKeywords(String userId) throws IOException {
        // 부스팅 필요
        SearchRequest request = SearchRequest.of(builder -> builder
            .index("user_search_logs")
            .size(0)
            .query(q -> q
                .bool(b -> b
                    .should(s -> s
                        .term(t -> t
                            .field("userId")
                            .value(userId)
                        )
                    )
                )
            )
            .aggregations("top_keywords",
                Aggregation.of(a -> a
                    .terms(TermsAggregation.of(t -> t
                        .field("searchText")
                        .size(3)
                    ))
                )
            )
        );

        SearchResponse<Void> response = esClient.search(request, Void.class);
        return parseKeywordAggregation(response, "top_keywords");
    }
    // 2. 일일 상위 검색어
    public List<KeywordCount> findDailyTopKeywords() throws IOException {
        ZonedDateTime oneDayAgo = ZonedDateTime.now().minusDays(1);
        System.out.println("oneDayAgo: " + oneDayAgo);
        SearchRequest request = getSearchRequestPopularSearchLogsByPeriod(oneDayAgo);

        SearchResponse<Void> response = esClient.search(request, Void.class);
        System.out.println("response: " + response);
        return parseKeywordAggregation(response, "top_keywords");
    }
    // 3. 주간 상위 검색어
    public List<KeywordCount> findWeeklyTopKeywords() throws IOException {
        ZonedDateTime oneWeekAgo = ZonedDateTime.now().minusWeeks(1);

        SearchRequest request = getSearchRequestPopularSearchLogsByPeriod(oneWeekAgo);

        SearchResponse<Void> response = esClient.search(request, Void.class);
        return parseKeywordAggregation(response, "top_keywords");
    }

    // 4. 월간 상위 검색어
    public List<KeywordCount> findMonthlyTopKeywords() throws IOException {
        ZonedDateTime oneMonthAgo = ZonedDateTime.now().minusMonths(1);

        SearchRequest request = getSearchRequestPopularSearchLogsByPeriod(oneMonthAgo);

        SearchResponse<Void> response = esClient.search(request, Void.class);
        return parseKeywordAggregation(response, "top_keywords");
    }

    // 일일, 주간, 월간 검색어 집계를 위한 검색 요청 생성
    // 인기 검색 로그 기간 조회
    public SearchRequest getSearchRequestPopularSearchLogsByPeriod(ZonedDateTime from) {
        // from ~ 현재 : ( 2025-01-01 ~ 현재의 데이터)
        System.out.println("데이트 :" + from.toString());
        return SearchRequest.of(builder -> builder
            .index("user_search_logs")
            .size(0)
            .query(Query.of(q -> q
                .range(r -> r
                    .date(d -> d
                        .field("timestamp")
                        .gte(from.toString())
                        .format("strict_date_optional_time||epoch_millis")
                        .timeZone("Asia/Seoul")
                    )
                )
            ))
//            .aggregations("top_keywords",
//                Aggregation.of(a -> a
//                    .terms(TermsAggregation.of(t -> t
//                        .field("keyword")
//                        .size(3)
//                    ))
//                )
//            )
        );
    }

    // 집계 결과 파싱 유틸리티 메서드
    private List<KeywordCount> parseKeywordAggregation(SearchResponse<Void> response, String aggregationName) {
        return response.aggregations()
            .get(aggregationName)
            .sterms()
            .buckets()
            .array()
            .stream()
            .map(bucket -> new KeywordCount(bucket.key().stringValue(), bucket.docCount()))
            .toList();
    }

    public List<UserSearchLogEs> findAll() {

        return searchLogEsDataRepository.findAll();
    }

    public UserSearchLogEs findById(String id) {
        return searchLogEsDataRepository.findById(id).orElseThrow();

    }
}

