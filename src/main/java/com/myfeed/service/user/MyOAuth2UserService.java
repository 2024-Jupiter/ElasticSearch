package com.myfeed.service.user;

import com.myfeed.model.user.LoginProvider;
import com.myfeed.model.user.MyUserDetails;
import com.myfeed.model.user.Role;

import com.myfeed.model.user.User;
import java.time.LocalDateTime;

import com.myfeed.model.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


import java.util.Map;

@Service
@Slf4j
public class MyOAuth2UserService extends DefaultOAuth2UserService {
    @Autowired private UserService userService;
    @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String email, uname, profileUrl;
        String hashedPwd = bCryptPasswordEncoder.encode("Social Login");
        User user = null;

        String accessToken = userRequest.getAccessToken().getTokenValue();

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        switch (provider) {
            // public User(String email, String password, String username, String nickname, String profileImage, LoginProvider loginProvider) {
            case "kakao":
                Map<String, Object> account = (Map) oAuth2User.getAttribute("kakao_account");
                email = "kakao_"+ account.get("email");
                user = userService.findByEmail(email);

                if (user == null) {
                    long kid = (long) oAuth2User.getAttribute("id");
                    Map<String, String> properties = (Map) oAuth2User.getAttribute("properties");
                    account = (Map) oAuth2User.getAttribute("kakao_account");
                    String nickname = (String) properties.get("nickname");
                    nickname = (nickname == null) ? "k_"+kid : nickname;
                    profileUrl = (String) properties.get("profile_image");
                    user = User.builder()
                                    .email(email)
                                    .password(hashedPwd)
                                    .username(nickname)
                                    .nickname(nickname).role(Role.USER)
                                    .isActive(true)
                                    .profileImage(profileUrl)
                                    .loginProvider(LoginProvider.KAKAO)
                                    .build();
                    userService.registerUser(user);
                }
                break;

            case "google":
                email = oAuth2User.getAttribute("email");
                user = userService.findByEmail(email);
                if (user == null) {
                    uname = oAuth2User.getAttribute("name");
                    String sub = oAuth2User.getAttribute("sub");
                    uname = (uname == null) ? "g_"+sub : uname;
                    email = oAuth2User.getAttribute("email");
                    profileUrl = oAuth2User.getAttribute("picture");
                    user = User.builder()
                            .email(email)
                            .password(hashedPwd)
                            .username(uname)
                            .nickname(uname)
                            .role(Role.USER)
                            .isActive(true)
                            .profileImage(profileUrl)
                            .loginProvider(LoginProvider.GOOGLE)
                            .build();
                    userService.registerUser(user);
                    log.info("구글 계정을 통해 회원가입이 되었습니다.: " + user.getUsername());
                }
                break;

            case "github": // 깃허브 이메일 필수값 아님..
                int id = oAuth2User.getAttribute("id");
                email = oAuth2User.getAttribute("email");
                email = (email == null) ? id+"@myfeed.com": email;
                user = userService.findByEmail(email);

                if (user == null) {
                    uname = oAuth2User.getAttribute("login");
                    uname = (uname == null) ? "g_"+id : uname;
                    profileUrl = oAuth2User.getAttribute("avatar_url");
                    user = User.builder()
                            .email(email)
                            .password(hashedPwd)
                            .username(uname)
                            .nickname(uname)
                            .role(Role.USER)
                            .isActive(true)
                            .profileImage(profileUrl)
                            .loginProvider(LoginProvider.GITHUB)
                            .build();
                    userService.registerUser(user);
                    log.info("깃허브 계정을 통해 회원가입이 되었습니다. " + user.getUsername());
                }
                break;


        }
        return new MyUserDetails(user, oAuth2User.getAttributes());
    }
}
