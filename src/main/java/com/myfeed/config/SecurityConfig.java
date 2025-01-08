package com.myfeed.config;


import com.myfeed.jwt.JwtRequestFilter;
import com.myfeed.model.user.Role;
import com.myfeed.service.user.MyOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private AuthenticationSuccessHandler authSuccessHandler;
    @Autowired
    private AuthenticationFailureHandler failureHandler;
    @Autowired
    private MyOAuth2UserService myOAuth2UserService;
    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(auth -> auth.disable())       // CSRF 방어 기능 비활성화
                .headers(x -> x.frameOptions(y -> y.disable()))     // H2-console
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/send-sms/**", "/error").permitAll()
                        .requestMatchers("/login", "/api/users/find-id" ,"/api/users/find-password" ,"/api/users/check-email","/api/users/check-nickname", "/api/users/custom-login", "/api/users/register", "/api/posts/detail/*", "api/replies/posts/detail/*" ,"/api/postEs/**", "/api/users/*/detail", "/api/users/*", "/view/home").permitAll()
                        .requestMatchers("/login/oauth2/code/google","auth/google/callback","/auth/kakao/callback", "/login/oauth2/code/**").permitAll()
                        .requestMatchers("/css/**","/js/**","/lib/**","/scss/**", "/img/**", "/favicon.ico" ).permitAll()
                        .requestMatchers("/api/posts/**", "/api/replies/**").hasAuthority(String.valueOf(Role.USER)) // 로그인한 사용자만 사용 가능 (개시글 목록 보기랑 게시글 보기는 비로그인도 가능)
                        .requestMatchers("/api/admin/users/**", "/api/admin/users", "/api/admin/reports/**").hasAuthority(String.valueOf(Role.ADMIN))
                        .anyRequest().authenticated()
                )
                .formLogin(auth -> auth
                        .loginPage("/api/users/custom-login") // template return url users/loginPage
                        .loginProcessingUrl("/api/users/custom-login")  // post 엔드포인트
                        .usernameParameter("email")
                        .passwordParameter("pwd")
                        //.defaultSuccessUrl("/api/users/loginSuccess", false)
                        .successHandler(authSuccessHandler)
                        .failureHandler(failureHandler)
                        .permitAll()
                )
                .logout(auth -> auth
                        .logoutUrl("/api/users/logout")
                        //.invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .deleteCookies("accessToken")
                        .logoutSuccessUrl("/api/users/test")
                )
                .oauth2Login(auth -> auth
                        .userInfoEndpoint(user -> user.userService(myOAuth2UserService))
                        .successHandler(authSuccessHandler)
                        .failureHandler(failureHandler)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ); // 세션 비활성화

        // JwtRequestFilter 추가
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
