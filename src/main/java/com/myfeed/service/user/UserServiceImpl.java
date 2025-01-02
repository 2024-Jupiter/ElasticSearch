package com.myfeed.service.user;

import com.myfeed.model.user.UpdateDto;
import com.myfeed.model.user.User;
import com.myfeed.repository.jpa.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public List<User> findByUsernameAndPhoneNumber(String username, String phoneNumber) {
        return userRepository.findByUsernameAndPhoneNumber(username, phoneNumber);
    }

    @Override
    public List<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User findByNickname(String nickname) {
        return userRepository.findByNickname(nickname).orElse(null);
    }

    @Override
    public void registerUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void updateUser(Long id, UpdateDto updateDto) {
        User user = findById(id);
        String hashedPwd = BCrypt.hashpw(updateDto.getPwd(), BCrypt.gensalt());
        user.setPassword(hashedPwd);
        user.setUsername(updateDto.getUname());
        user.setNickname(updateDto.getNickname());
        user.setProfileImage(updateDto.getProfileImage());
        userRepository.save(user);
    }

    @Override
    public void updateUserStatus(Long id, boolean status) {
        User user = findById(id);
        if (user.isActive() != status) {
            user.setActive(status);
            userRepository.save(user);
        }
    }

    @Override
    public void setTempPassword(String email, String tempPassword) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.setPassword(tempPassword);
            userRepository.save(user);
        }
    }

    @Override
    public void deleteUser(Long uid) { // soft delete
        User user = findById(uid);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public Page<User> getPagedUser(int page, boolean isActive) {
        Pageable pageable = PageRequest.of(page-1,PAGE_SIZE);
        Page<User> userPage = null;
        if (isActive) {
            userPage = userRepository.findAllByActiveTrue(pageable);
        } else {
            userPage = userRepository.findAllByActiveFalse(pageable);
        }
        return null;
    }
}
