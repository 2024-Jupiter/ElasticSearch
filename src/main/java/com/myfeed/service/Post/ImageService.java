package com.myfeed.service.Post;

import com.myfeed.model.post.Post;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    void uploadImage(Post post, List<MultipartFile> images) throws IOException;

    String saveImage(MultipartFile image, String uploadsDir) throws IOException;
}
