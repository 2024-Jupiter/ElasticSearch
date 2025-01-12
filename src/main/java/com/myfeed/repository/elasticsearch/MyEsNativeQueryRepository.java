package com.myfeed.repository.elasticsearch;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Repository;

@Repository
public class MyEsNativeQueryRepository {

    // 1달간의 인기 검색어를 가져오는 쿼리 최근 검색어를 더 가중치를 주어서 가져옴
    public NativeQuery getPopularKeywordDuringMonthQuery() {
        return NativeQuery.builder()
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
    }


    // 나의 1달간의 검색 로그 중 인기 검색어를 가져오는 쿼리
    public NativeQuery getMyAllTimeTopKeywords(String userId) {
        String query = String.format("""
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
                  },
                  {
                    "term": {
                      "userId": "%s"
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
        """, userId);
        return NativeQuery.builder()
            .withQuery(new StringQuery(query)).withAggregation("top_keywords",
                Aggregation.of(a -> a
                    .terms(t -> t
                        .field("searchText.keyword")
                        .size(3)
                    ))
            )
            .build();

    }
    // 전체 이용자의 인기 검색어를 가져오는 쿼리
    public NativeQuery getPopularSearchLogsQuery(){
        return NativeQuery.builder()
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
    }
}
