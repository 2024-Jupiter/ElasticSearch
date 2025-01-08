package com.myfeed.repository.jpa;

import com.myfeed.model.post.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
