package com.myfeed.jwt;

import com.myfeed.model.user.User;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;


@Getter
public class AccountAdapter extends org.springframework.security.core.userdetails.User {

    private User user;

    public AccountAdapter(User user) {
        super(user.getEmail(), user.getPassword(), List.of(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole().toString();
            }
        }));
        this.user = user;
    }

    @Override // 비활성회원 로그인 차단
    public boolean isEnabled() {
        return user.isActive();
    }
}