package com.myfeed.service.Post;

import com.myfeed.model.post.Post;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    // 이미지 업로드
    void uploadImage(Post post, List<MultipartFile> images) throws IOException;

    // 이미지 수정 & 삭제
    void updateImages(Post post, List<MultipartFile> newImages) throws IOException;

    // 이미지 저장
    String saveImage(MultipartFile image, String uploadsDir) throws IOException;
}
