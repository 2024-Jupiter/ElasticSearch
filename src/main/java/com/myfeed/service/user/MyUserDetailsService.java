package com.myfeed.service.user;

import com.myfeed.jwt.AccountAdapter;
import com.myfeed.model.user.LoginProvider;
import com.myfeed.model.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserService userService;

    // 폼로그인 예외처리
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.findByEmail(email);

        // soft deleted된 유저 차단
        if (user == null || user.getLoginProvider().equals(LoginProvider.FORM) && user.isDeleted()) {
            // log.warn("form Login 실패: 이메일을 찾을 수 없습니다. (user email: " + email + ")");
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
        }

//        log.info("form Login 시도: " + user.getNickname());
//        return new MyUserDetails(user);
        return new AccountAdapter(user);
    }

}
