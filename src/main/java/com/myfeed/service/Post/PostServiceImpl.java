package com.myfeed.service.Post;

import com.myfeed.exception.*;
import com.myfeed.model.post.*;
import com.myfeed.model.user.Role;
import com.myfeed.model.user.User;
import com.myfeed.response.ErrorCode;
import com.myfeed.sync.PostSyncEvent;
import com.myfeed.repository.jpa.PostRepository;
import com.myfeed.repository.jpa.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class PostServiceImpl implements PostService {
    @Autowired PostRepository postRepository;
    @Autowired UserRepository userRepository;
    @Autowired private ApplicationEventPublisher eventPublisher;

    // 게시글 가져 오기
    @Override
    public Post findPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.POST_NOT_FOUND));
    }



    // 게시글 작성 (postEs로 post 전달)
    @Transactional
    @Override
    public Long createPost(Long userId, PostDto postDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new  ExpectedException(ErrorCode.USER_NOT_FOUND));

        if (postDto.getCategory().equals(Category.NEWS) && user.getRole().equals(Role.USER)) {
            throw new ExpectedException(ErrorCode.ACCESS_DENIED);
        }

        Post post = Post.builder()
                .user(user).title(postDto.getTitle()).content(postDto.getContent())
                .category(postDto.getCategory()).status(BlockStatus.NORMAL_STATUS)
                .viewCount(0).likeCount(0)
                .build();

//        if (!postDto.getImages().isEmpty()) {
//            for (ImageDto imageDto : postDto.getImages()) {
//                if (!isValidImageFormat(imageDto)) {
//                    throw new ExpectedException(ErrorCode.WRONG_IMAGE_FILE);
//                }
//            }
//        }

//        List<Image> images = convertImageDtosToImages(postDto.getImages(), post);
//        for (Image image: images) {
//            post.addImage(image);
//        }

        Post savedPost = postRepository.save(post);
        eventPublisher.publishEvent(new PostSyncEvent(savedPost.getId(), "CREATE_OR_UPDATE"));

        return savedPost.getId();
    }

    // 이미지 형식 확인
    private boolean isValidImageFormat(ImageDto imageDto) {
        String imageUrl = imageDto.getImageSrc();
        return (imageUrl.endsWith(".jpg") || imageUrl.endsWith(".png"));
    }

    // ImageDto -> Image 변환
    private List<Image> convertImageDtosToImages(List<ImageDto> imageDtos, Post post) {
        List<Image> images = new ArrayList<>();
        for (ImageDto imageDto : imageDtos) {
            Image image = new Image();
            image.setImageSrc(imageDto.getImageSrc());
            image.setPost(post);
            images.add(image);
        }
        return images;
    }

    // 게시글 수정 (postEs로 post 전달)
    @Transactional
    @Override
    public void updatePost(Long id, User user, UpdateDto updateDto) {
        Post post = findPostById(id);

        if (post.getStatus() == BlockStatus.BLOCK_STATUS) {
            throw new  ExpectedException(ErrorCode.REPLY_BLOCKED);
        }

//        List<Image> updatedImages = convertImageDtosToImages(updateDto.getImages(), post);
//
        post.setTitle(updateDto.getTitle());
        post.setContent(updateDto.getContent());
//        post.setImages(updatedImages);
//
//        if (!updateDto.getImages().isEmpty()) {
//            for (ImageDto imageDto : updateDto.getImages()) {
//                if (!isValidImageFormat(imageDto)) {
//                    throw new ExpectedException(ErrorCode.WRONG_IMAGE_FILE);
//                }
//            }
//        }

        Post savedPost = postRepository.save(post);
        eventPublisher.publishEvent(new PostSyncEvent(savedPost.getId(), "CREATE_OR_UPDATE"));
    }

    // 게시글 삭제
    @Transactional
    @Override
    public void deletePostById(Long id, User user) {
        postRepository.deleteById(id);
        eventPublisher.publishEvent(new PostSyncEvent(id, "DELETE"));
    }

    // 전체 게시글 페이지 네이션 (동시성)
    @Override
    public Page<Post> getPagedPosts(int page) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("updatedAt").descending());
        Page<Post> posts = postRepository.findAll(pageable);

        for (Post post : posts) {
            if (post.getStatus() == BlockStatus.BLOCK_STATUS) {
                throw new ExpectedException(ErrorCode.INCLUDED_BLOCK_POST);
            }
        }
        return posts;
    }
    // 내 게시글 페이지 네이션
    @Override
    public Page<Post> getPagedPostsByUserId(int page, Long userId) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("updatedAt").descending());
        Page<Post> posts = postRepository.findPagedPostsByUserId(pageable, userId);

        for (Post post : posts) {
            if (post.getStatus() == BlockStatus.BLOCK_STATUS) {
                throw new ExpectedException(ErrorCode.INCLUDED_BLOCK_POST);
            }
        }

        return posts;
    }

    // 조회수 증가 (동시성)
    @Transactional
    @Override
    public void incrementPostViewCountById(Long id) {
        postRepository.updateViewCountById(id);
    }

    // 좋아요 증가 (동시성)
    @Transactional
    @Override
    public void incrementPostLikeCountById(Long id) {
        postRepository.updateLikeCountById(id);
    }

    // 좋아요 감소 (동시성)
    @Transactional
    @Override
    public void decrementPostLikeCountById(Long id) {
        postRepository.decrementLikeCountById(id);
    }
}
