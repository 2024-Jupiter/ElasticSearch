package com.myfeed.service.user;

import com.myfeed.model.user.UpdateDto;
import com.myfeed.model.user.User;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.data.domain.Page;

public interface UserService {
    public static final int PAGE_SIZE = 20;

    User findById(Long uid);

    List<User> findByUsernameAndPhoneNumber(String username, String phoneNumber);

    List<User> findByPhoneNumber(String phoneNumber);

    User findByEmail(String email);

    User findByNickname(String nickname);

    void updateUser(Long id, UpdateDto updateDto);

    void updateUserStatus(Long id, boolean status);

    void registerUser(User user);

    void deleteUser(Long uid);

    void deleteUserAccessToken(HttpServletResponse response);

    void setTempPassword(String email, String tempPassword);

    Page<User> getPagedUser(int page, boolean isActive);
}
