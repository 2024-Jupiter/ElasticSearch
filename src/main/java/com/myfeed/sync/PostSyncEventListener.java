package com.myfeed.sync;

import com.myfeed.exception.ExpectedException;
import com.myfeed.model.elastic.post.PostEs;
import com.myfeed.model.post.Post;
import com.myfeed.model.user.User;
import com.myfeed.repository.jpa.PostRepository;
import com.myfeed.repository.jpa.UserRepository;
import com.myfeed.response.ErrorCode;
import com.myfeed.service.Post.PostEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PostSyncEventListener {
    @Autowired private PostEsService postEsService;
    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;

    @Async
    @EventListener
    public void handlePostSyncEvent(PostSyncEvent event) {
        if ("CREATE_OR_UPDATE".equals(event.getOperation())) {
            Post post = postRepository.findById(event.getPostId()).orElseThrow(() -> new ExpectedException(ErrorCode.POST_NOT_FOUND));
            User user = userRepository.findById(post.getUser().getId()).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

            PostEs postEs = new PostEs();
            postEs.setId(generateNewIdForPost(post)); // id 중복 문제 해결
            postEs.setNickname(user.getNickname());
            postEs.setTitle(post.getTitle());
            postEs.setContent(post.getContent());
            postEs.setCategory(post.getCategory());
            postEs.setViewCount(post.getViewCount());
            postEs.setLikeCount(post.getLikeCount());
            postEsService.syncToElasticsearch(postEs);
        } else if ("DELETE".equals(event.getOperation())) {
            postEsService.deleteFromElasticsearch(String.valueOf(event.getPostId()));
        }
    }

    // 동시성
    private String generateNewIdForPost(Post post) {
        // UUID를 사용하여 고유한 ID를 생성
        return "post-" + post.getId() + "-" + UUID.randomUUID().toString();
    }
}