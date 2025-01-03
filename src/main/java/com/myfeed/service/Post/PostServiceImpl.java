package com.myfeed.service.Post;

import com.myfeed.exception.*;
import com.myfeed.model.post.*;
import com.myfeed.model.user.Role;
import com.myfeed.model.user.User;
import com.myfeed.sync.PostSyncEvent;
import com.myfeed.repository.jpa.PostRepository;
import com.myfeed.repository.jpa.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
        return postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
    }

    // 게시글의 사용자 아이디 가져 오기
    @Override
    public List<User> getUsersById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (user == null || user.isDeleted()) {
            throw new UserDeletedException("삭제된 사용자의 게시글이 포함 되어 있습니다.");
        }

        return postRepository.findUsersById(userId);
    }

    // 게시글 작성 (postEs로 post 전달)
    @Transactional
    @Override
    public Post createPost(Long userId, PostDto postDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (postDto.getCategory().equals(Category.NEWS) && user.getRole().equals(Role.USER)) {
            throw new AccessDeniedException("이 작업은 관리자만 수행할 수 있습니다.");
        }

        Post post = Post.builder()
                .user(user).title(postDto.getTitle()).content(postDto.getContent())
                .category(postDto.getCategory())
                .build();

        if (!postDto.getImages().isEmpty()) {
            for (ImageDto imageDto : postDto.getImages()) {
                if (!isValidImageFormat(imageDto)) {
                    throw new ImageUploadException("잘못된 이미지 형식 입니다.");
                }
            }
        }

        List<Image> images = convertImageDtosToImages(postDto.getImages(), post);
        for (Image image: images) {
            post.addImage(image);
        }

        Post savedPost = postRepository.save(post);
        eventPublisher.publishEvent(new PostSyncEvent(savedPost.getId(), "CREATE_OR_UPDATE"));

        return savedPost;
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
    public Post updatePost(Long id, UpdateDto updateDto) {
        Post post = findPostById(id);

        if (post.getStatus() == BlockStatus.BLOCK_STATUS) {
            throw new PostBlockedException("차단된 게시글 입니다.");
        }

        List<Image> updatedImages = convertImageDtosToImages(updateDto.getImages(), post);

        post.setTitle(updateDto.getTitle());
        post.setContent(updateDto.getContent());
        post.setImages(updatedImages);

        if (!updateDto.getImages().isEmpty()) {
            for (ImageDto imageDto : updateDto.getImages()) {
                if (!isValidImageFormat(imageDto)) {
                    throw new ImageUploadException("잘못된 이미지 형식 입니다.");
                }
            }
        }

        Post savedPost = postRepository.save(post);
        eventPublisher.publishEvent(new PostSyncEvent(savedPost.getId(), "CREATE_OR_UPDATE"));

        return savedPost;
    }

    // 게시글 삭제
    @Transactional
    @Override
    public void deletePostById(Long id) {
        postRepository.deleteById(id);
        eventPublisher.publishEvent(new PostSyncEvent(id, "DELETE"));
    }

    // 내 게시글 페이지 네이션
    @Override
    public Page<Post> getPagedPostsByUserId(int page,User user) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("createdDate").descending());
        Page<Post> posts = postRepository.findPagedPostsByUserId(user, pageable);

        for (Post post : posts) {
            if (post.getStatus() == BlockStatus.BLOCK_STATUS) {
                throw new ReplyBlockedException("차단된 게시글이 포함 되어 있습니다.");
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
