package com.myfeed.service.reply;

import com.myfeed.model.post.Post;
import com.myfeed.model.reply.Reply;
import com.myfeed.model.reply.ReplyDto;
import com.myfeed.model.user.User;
import org.springframework.data.domain.Page;

public interface ReplyService {
    public static final int PAGE_SIZE = 10;

    // 댓글 가져 오기
    Reply findByReplyId(Long id);

    // 댓글 작성
    Long createReply(Long userId, Long postId, ReplyDto replyDto);

    // 게시글 내의 댓글 리스트 (동시성)
    Page<Reply> getPagedRepliesByPost(int page, Long postId);

    // 댓글 수정
    void updateReply(Long id, User user ,ReplyDto replyDto);

    // 댓글 삭제
    void deleteReply(Long id, User user);
}
