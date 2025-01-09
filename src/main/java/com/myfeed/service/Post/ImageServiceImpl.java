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

    // 이미지 업로드
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
            // 관계 설정
            post.addImage(img);
            // imageRepository.save(img);
        }
    }

    // 이미지 수정 & 삭제
    @Override
    public void updateImages(Post post, List<MultipartFile> newImages) throws IOException {
        // 기존 이미지 목록 가져 오기
        List<Image> existingImages = post.getImages();

        // 새로 추가된 이미지 업로드 및 관계 설정
        if (!newImages.isEmpty()) {
            // 이미지 업로드 및 관계 설정
            for (MultipartFile imageFile : newImages) {
                String dbFilePath = saveImage(imageFile, "src/main/resources/static/posts/images");

                // 기존 이미지와 비교하여 중복된 이미지가 없다면 추가
                boolean isDuplicate = false;
                for (Image existingImage : existingImages) {
                    if (existingImage.getImageSrc().equals(dbFilePath)) {
                        isDuplicate = true;
                        break;
                    }
                }

                if (!isDuplicate) {
                    Image image = new Image(post, dbFilePath);
                    post.addImage(image); // 관계 설정
                }
            }
        }

        // 삭제된 이미지 처리 (새로운 이미지 목록에 없는 기존 이미지를 삭제)
        for (Image existingImage : existingImages) {
            boolean isRemoved = true;
            for (MultipartFile newImageFile : newImages) {
                String newImagePath = saveImage(newImageFile, "src/main/resources/static/posts/images");
                if (existingImage.getImageSrc().equals(newImagePath)) {
                    isRemoved = false;
                    break;
                }
            }

            if (isRemoved) {
                // 기존 이미지를 리스트에서 제거하고 DB에서 삭제
                post.getImages().remove(existingImage);
                imageRepository.delete(existingImage);
            }
        }
    }

    // 이미지 저장
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
