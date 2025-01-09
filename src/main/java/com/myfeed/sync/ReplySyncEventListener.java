package com.myfeed.sync;

import com.myfeed.exception.ExpectedException;
import com.myfeed.model.elastic.post.PostEs;
import com.myfeed.model.reply.Reply;
import com.myfeed.model.reply.ReplyEs;
import com.myfeed.repository.elasticsearch.PostEsDataRepository;
import com.myfeed.repository.jpa.ReplyRepository;
import com.myfeed.response.ErrorCode;
import com.myfeed.service.Post.PostEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReplySyncEventListener {
    @Autowired private PostEsDataRepository postEsDataRepository;
    @Autowired private PostEsService postEsService;
    @Autowired private ReplyRepository replyRepository;

    // 댓글 작성
    @Async
    //@EventListener
    @TransactionalEventListener
    public void handleReplySyncEvent(ReplySyncEvent event) {
        // 댓글 작성 & 수정
        if ("CREATE_OR_UPDATE".equals(event.getOperation())) {
            Reply reply = replyRepository.findById(event.getReplyId()).orElseThrow(() -> new ExpectedException(ErrorCode.REPLY_NOT_FOUND));
            PostEs postEs = postEsDataRepository.findById(String.valueOf(reply.getPost().getId())).orElseThrow(() -> new ExpectedException(ErrorCode.POST_ES_NOT_FOUND));

            postEs.setNickname(postEs.getNickname());
            postEs.setTitle(postEs.getTitle());
            postEs.setContent(postEs.getContent());
            postEs.setCategory(postEs.getCategory());
            postEs.setViewCount(postEs.getViewCount());
            postEs.setLikeCount(postEs.getLikeCount());
            postEs.setCreatedAt(postEs.getCreatedAt());

            List<ReplyEs> replies = replyRepository.findByPostId(reply.getPost().getId()).stream()
                    .map(replyItem -> {
                        ReplyEs replyEs = new ReplyEs();
                        replyEs.setId(String.valueOf(replyItem.getId()));
                        replyEs.setNickname(replyItem.getUser().getNickname());
                        replyEs.setContent(replyItem.getContent());
                        replyEs.setCreatedAt(replyItem.getCreatedAt());
                        return replyEs;
                    })
                    .collect(Collectors.toList());

            postEs.setReplies(replies);
            postEs.setReplyCount(replies.size());

            postEsService.syncToElasticsearch(postEs);
        } else if ("DELETE".equals(event.getOperation())) { // 댓글 삭제
            Reply reply = replyRepository.findById(event.getReplyId()).orElseThrow(() -> new ExpectedException(ErrorCode.REPLY_NOT_FOUND));
            PostEs postEs = postEsDataRepository.findById(String.valueOf(reply.getPost().getId())).orElseThrow(() -> new ExpectedException(ErrorCode.POST_ES_NOT_FOUND));

            postEs.getReplies().removeIf(replyEs -> String.valueOf(replyEs.getId()).equals(String.valueOf(event.getReplyId())));

            // 댓글 삭제
            replyRepository.deleteById(event.getReplyId());
            postEsService.syncToElasticsearch(postEs);
        }
    }
}
