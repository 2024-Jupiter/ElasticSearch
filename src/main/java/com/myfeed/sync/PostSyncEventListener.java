package com.myfeed.sync;

import com.myfeed.exception.ExpectedException;
import com.myfeed.model.elastic.post.PostEs;
import com.myfeed.model.post.Post;
import com.myfeed.model.user.User;
import com.myfeed.repository.elasticsearch.PostEsDataRepository;
import com.myfeed.repository.jpa.PostRepository;
import com.myfeed.repository.jpa.ReplyRepository;
import com.myfeed.repository.jpa.UserRepository;
import com.myfeed.response.ErrorCode;
import com.myfeed.service.Post.PostEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;

@Component
public class PostSyncEventListener {
    @Autowired private PostEsService postEsService;
    @Autowired private PostEsDataRepository postEsDataRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ReplyRepository replyRepository;

    // 게시글 작성
    @Async
    //@EventListener
    @TransactionalEventListener
    public void handlePostSyncEvent(PostSyncEvent event) {
        if ("CREATE_OR_UPDATE".equals(event.getOperation())) {
            Post post = postRepository.findById(event.getPostId()).orElseThrow(() -> new ExpectedException(ErrorCode.POST_NOT_FOUND));
            User user = userRepository.findById(post.getUser().getId()).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

            PostEs postEs = new PostEs();
            postEs.setId(String.valueOf(post.getId())); // id 중복 문제 해결
            postEs.setNickname(user.getNickname());
            postEs.setTitle(post.getTitle());
            postEs.setContent(post.getContent());
            postEs.setCategory(post.getCategory());
            postEs.setViewCount(post.getViewCount());
            postEs.setLikeCount(post.getLikeCount());
            postEs.setCreatedAt(post.getCreatedAt());
            postEs.setReplies(new ArrayList<>());

            postEsService.syncToElasticsearch(postEs);
        } else if ("DELETE".equals(event.getOperation())) {
            postEsService.deleteFromElasticsearch(String.valueOf(event.getPostId()));
        } else if ("VIEW_COUNT_UP_AND_LIKE_COUNT_UP".equals(event.getOperation())) {
            PostEs postEs = postEsDataRepository.findById(String.valueOf(event.getPostId())).orElseThrow(() -> new ExpectedException(ErrorCode.POST_ES_NOT_FOUND));

            postEs.setId(String.valueOf(postEs.getId()));
            postEs.setNickname(postEs.getNickname());
            postEs.setTitle(postEs.getTitle());
            postEs.setContent(postEs.getContent());
            postEs.setCategory(postEs.getCategory());
            postEs.setCreatedAt(postEs.getCreatedAt());
            int viewCount = postEs.getViewCount();
            viewCount += 1;
            postEs.setViewCount(viewCount);
            int likeCount = postEs.getLikeCount();
            likeCount += 1;
            postEs.setLikeCount(likeCount);

            if (postEs.getReplies() == null) {
                postEs.setReplies(new ArrayList<>());
            } else {
                postEs.setReplies(postEs.getReplies());
                postEs.setReplyCount(postEs.getReplyCount());
            }

            postEsService.syncToElasticsearch(postEs);
        } else if ("VIEW_COUNT_UP_AND_LIKE_COUNT_DOWN".equals(event.getOperation())) {
            PostEs postEs = postEsDataRepository.findById(String.valueOf(event.getPostId())).orElseThrow(() -> new ExpectedException(ErrorCode.POST_ES_NOT_FOUND));

            postEs.setId(String.valueOf(postEs.getId()));
            postEs.setNickname(postEs.getNickname());
            postEs.setTitle(postEs.getTitle());
            postEs.setContent(postEs.getContent());
            postEs.setCategory(postEs.getCategory());
            postEs.setCreatedAt(postEs.getCreatedAt());
            int viewCount = postEs.getViewCount();
            viewCount += 1;
            postEs.setViewCount(viewCount);
            int likeCount = postEs.getLikeCount();
            if (likeCount > 0) {
                likeCount -= 1;
            }
            postEs.setLikeCount(likeCount);

            if (postEs.getReplies() == null) {
                postEs.setReplies(new ArrayList<>());
            } else {
                postEs.setReplies(postEs.getReplies());
                postEs.setReplyCount(postEs.getReplyCount());
            }

            postEsService.syncToElasticsearch(postEs);
        }
    }
}