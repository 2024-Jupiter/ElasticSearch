package com.myfeed.repository.elasticsearch;

import com.myfeed.model.elastic.post.PostEs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.annotations.SourceFilters;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostEsDataRepository extends ElasticsearchRepository<PostEs, String> {
    // 페이지네이션
    Page<PostEs> findAll(Pageable pageable);

    // 1. 제목 통합 검색
    @Query("""
    {
      "bool": {
        "must": [
          { "match": { "title": "?0" } }
        ]
      }
    }
    """)
    Page<PostEs> searchByTitle(String keyword, Pageable pageable);

    // 1-1. 제목+카테고리 검색
    @SourceFilters(excludes = "content")
    @Query("""
            {
              "bool": {
                "must": [
                  { "match": { "title": "?0" } },
                  { "term": { "category": "?1" } }
                ]
              }
           
            }
            """)
    Page<PostEs> searchByTitleAndCategory(String keyword, String category, Pageable pageable);


    // 2. 내용 통합 검색
    @Query("""
    {
      "bool": {
        "must": [
          { "match": { "content": "?0" } }
        ]
      }
    }
    """)
    Page<PostEs> searchByContent(String keyword , Pageable pageable);

    // 2. 내용 검색 + 카테고리 검색
    @SourceFilters(excludes = "content")
    @Query("""
            {
              "bool": {
                "must": [
                  { "match": { "content": "?0" } },
                  { "term": { "category": "?1" } }
                ]
              }
            }
            """)
    Page<PostEs> searchByContentAndCategory(String keyword, String category , Pageable pageable);


    // 3.  제목+내용 검색 (OR 조건) + 카테고리 검색
    @SourceFilters(excludes = "content")
    @Query("""
        {
         "bool": {
           "must": {
             "multi_match": {
               "query": "?0",
               "fields": ["title^1.4", "content^1.1"]
             }
           },
           "filter": {
             "term": {
               "category": "?1"
             }
           }
         }
        }
    """)
    Page<PostEs> searchTitleAndContent(String keyword, String category, Pageable pageable);


    @SourceFilters(excludes = "content")
    Page<PostEs> findAllByOrderByCreatedAtDesc(Pageable pr);
}
