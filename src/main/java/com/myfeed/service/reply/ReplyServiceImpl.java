package com.myfeed.service.reply;

import com.myfeed.exception.ExpectedException;
import com.myfeed.model.post.BlockStatus;
import com.myfeed.model.reply.Reply;
import com.myfeed.model.post.Post;
import com.myfeed.model.reply.ReplyDto;
import com.myfeed.model.user.User;
import com.myfeed.repository.jpa.ReplyRepository;
import com.myfeed.repository.jpa.PostRepository;
import com.myfeed.repository.jpa.UserRepository;
import com.myfeed.response.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReplyServiceImpl implements ReplyService {
    @Autowired UserRepository userRepository;
    @Autowired PostRepository postRepository;
    @Autowired ReplyRepository replyRepository;

    // 댓글 가져 오기
    @Override
    public Reply findByReplyId(Long id) {
        return replyRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.POST_NOT_FOUND));
    }

    // 댓글 작성
    @Override
    public Reply createReply(Long userId, Long postId, ReplyDto replyDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        if (post.getStatus() == BlockStatus.BLOCK_STATUS) {
            throw new ExpectedException(ErrorCode.REPLY_BLOCKED);
        }

        Reply reply = Reply.builder()
                .user(user).post(post).content(replyDto.getContent())
                .status(BlockStatus.NORMAL_STATUS)
                .build();
        post.addReply(reply);

        return replyRepository.save(reply);
    }

    // 게시글 내의 댓글 리스트 (동시성)
    @Override
    public Page<Reply> getPagedRepliesByPost(int page, Post post) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("updatedAt").descending());
        Page<Reply> replies = replyRepository.findPagedRepliesByPost(post, pageable);

        for (Reply reply : replies) {
            if (reply.getStatus() == BlockStatus.BLOCK_STATUS) {
                throw new ExpectedException(ErrorCode.INCLUDED_BLOCK_REPLY);
            }
            if (reply.getUser() == null || reply.getUser().isDeleted()) {
                throw new ExpectedException(ErrorCode.INCLUDED_DELETED_USER_IN_REPLY);
            }
        }

        List<Reply> filteredReplies = replies.getContent().stream()
                .filter(reply -> reply.getUser() != null && !reply.getUser().isDeleted())
                .toList();

        return new PageImpl<>(filteredReplies, pageable, replies.getTotalElements());
    }

    // 댓글 수정
    @Override
    public void updateReply (Long id, ReplyDto replyDto) {
        Reply reply = findByReplyId(id);

        if (reply.getPost().getStatus() == BlockStatus.BLOCK_STATUS) {
            throw new ExpectedException(ErrorCode.POST_BLOCKED);
        }
        if (reply.getStatus() == BlockStatus.BLOCK_STATUS) {
            throw new ExpectedException(ErrorCode.REPLY_BLOCKED);
        }

        reply.setContent(replyDto.getContent());
        replyRepository.save(reply);
    }

    // 댓글 삭제
    @Override
    public void deleteReply (Long id) {
         replyRepository.deleteById(id);
    }
}
