package com.myfeed.repository.elasticsearch;

import com.myfeed.model.elastic.UserSearchLogEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface SearchLogEsDataRepository extends ElasticsearchRepository<UserSearchLogEs, String> {

    List<UserSearchLogEs> findAll();
}
