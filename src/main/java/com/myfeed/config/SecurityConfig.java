package com.myfeed.config;


import com.myfeed.jwt.JwtRequestFilter;
import com.myfeed.model.user.Role;
import com.myfeed.service.user.MyOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
    @Autowired
    private CustomSecurityHandler customSecurityHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(auth -> auth.disable())       // CSRF 방어 기능 비활성화
                .headers(x -> x.frameOptions(y -> y.disable()))     // H2-console
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/send-sms/**", "/error").permitAll()
                        .requestMatchers("/login", "/api/users/find-id" ,"/api/users/find-password" ,"/api/users/check-email","/api/users/check-nickname", "/api/users/custom-login", "/api/users/register", "/api/users/*/detail", "/api/users/*", "/view/home").permitAll()
                        .requestMatchers("/oauth2/authorization/kakao","/login/oauth2/**","/login/oauth2/code/google","auth/google/callback","/auth/kakao/callback").permitAll()
                        .requestMatchers("/api/posts/detail/*", "api/replies/posts/detail/*" ,"/api/postEs/**" ).permitAll() // 게시글, 댓글 상세 보기 / 추천 게시글, 검색 게시글 목록 보기 및 상세 보기
                        .requestMatchers("/css/**","/js/**","/lib/**","/scss/**", "/img/**", "/favicon.ico" ).permitAll()
                        .requestMatchers("/api/posts/**", "/api/replies/**").hasAuthority(String.valueOf(Role.USER)) // 로그인한 사용자만 사용 가능
                        .requestMatchers(HttpMethod.POST, "/api/admin/reports/posts/*", "/api/admin/reports/replies/*").hasAuthority(String.valueOf(Role.USER))
                        .requestMatchers("/api/admin/users/**", "/api/admin/users", "/api/admin/reports/**").hasAuthority(String.valueOf(Role.ADMIN))
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(customSecurityHandler) // Handle unauthenticated users
                        .accessDeniedHandler(customSecurityHandler)      // Handle unauthorized access
                )
                .formLogin(auth -> auth
                        .loginPage("/api/users/test") // template return url users/loginPage
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
