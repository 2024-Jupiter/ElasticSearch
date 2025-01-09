package com.myfeed.repository.elasticsearch;

import com.myfeed.model.elastic.post.PostEs;
import com.myfeed.model.post.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class PostEsRepository {

    @Autowired
    private PostEsDataRepository postEsDataRepository;

    // 일반 게시글 제목 검색
    public Page<PostEs> searchGeneralPostsByTitle(String keyword, PageRequest pageRequest) {
        return postEsDataRepository.searchByTitleAndCategory(keyword, Category.GENERAL.toString() ,pageRequest);
    }

    // 일반 게시글 내용 검색
    public Page<PostEs> searchGeneralPostsByContent(String keyword, PageRequest pageRequest) {
        return postEsDataRepository.searchByContentAndCategory(keyword,Category.GENERAL.toString(), pageRequest);
    }

    // 일반 게시글 제목+내용 검색
    public Page<PostEs> searchGeneralPostsByTitleAndContent(String keyword, PageRequest pageRequest) {
        return postEsDataRepository.searchTitleAndContent(keyword, Category.GENERAL.toString(), pageRequest);
    }
}
