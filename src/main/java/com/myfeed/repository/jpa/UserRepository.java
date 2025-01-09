package com.myfeed.repository.jpa;

import java.util.List;
import java.util.Optional;

import com.myfeed.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUsernameAndPhoneNumber(String username, String phoneNumber);
    List<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);

    Page<User> findAllByIsActiveTrue(Pageable pageable);
    Page<User> findAllByIsActiveFalse(Pageable pageable);
}