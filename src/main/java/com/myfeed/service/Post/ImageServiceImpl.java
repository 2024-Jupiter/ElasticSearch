package com.myfeed.service.Post;

import com.myfeed.model.post.Image;
import com.myfeed.model.post.Post;
import com.myfeed.repository.jpa.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ImageServiceImpl implements ImageService {
    @Autowired ImageRepository imageRepository;

    @Override
    public void uploadImage(Post post, List<MultipartFile> images) throws IOException {
        // 이미지 파일 저장을 위한 경로 설정
        String uploadsDir = "src/main/resources/static/posts/images";

        // 각 이미지 파일에 대해 업로드 및 DB 저장 수행
        for (MultipartFile image : images) {
            // 이미지 파일 경로를 저장
            String dbFilePath = saveImage(image, uploadsDir);
            // ProductThumbnail 엔티티 생성 및 저장
            Image img = new Image(post, dbFilePath);
            imageRepository.save(img);
        }
    }

    @Override
    public String saveImage(MultipartFile image, String uploadsDir) throws IOException {
        // 파일 이름 생성
        String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + image.getOriginalFilename();
        // 실제 파일이 저장될 경로
        String filePath = uploadsDir + fileName;
        // DB에 저장할 경로 문자열
        String dbFilePath = "/posts/images/" + fileName;

        Path path = Paths.get(filePath); // Path 객체 생성
        Files.createDirectories(path.getParent()); // 디렉토리 생성
        Files.write(path, image.getBytes()); // 디렉토리에 파일 저장

        return dbFilePath;
    }
}
