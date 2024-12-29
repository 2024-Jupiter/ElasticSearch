package com.myfeed.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.myfeed.MyfeedApplication;
import com.myfeed.jwt.JwtRequestFilter;
import com.myfeed.jwt.JwtTokenUtil;
import com.myfeed.model.user.RegisterDto;
import com.myfeed.service.Post.PostService;
import com.myfeed.service.user.UserService;
import com.nimbusds.jose.shaded.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

@WebMvcTest(UserController.class) //
//@ContextConfiguration(classes = MyfeedApplication.class)
@AutoConfigureMockMvc(addFilters = false) // Security 필터 비활성화
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    private final Gson gson = new Gson();

    @MockBean
    private UserService userService;
    @MockBean
    private PostService postService;

    @DisplayName("회원가입 실패 - 이메일 형식이 아님")
    @Test
    void addUserFail_NotEmailFormat() throws Exception {
        // given
        final RegisterDto registerDto = RegisterDto.builder()
                .email("mangkyu")
                .pwd("password")
                .pwd2("password")
                .uname("혜란")
                .nickname("gPfks")
                .phoneNumber("1234")
                .build();

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/user/register")
                        .content(gson.toJson(registerDto))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("이메일 형식이 올바르지 않습니다.")) // JSON의 email 필드 확인
                .andDo(print()); // 요청 및 응답 로깅
    }

    @DisplayName("회원가입 실패 - 이름, 이메일, 닉네임 란이 null, 비어있고, 공백임")
    @Test
    void addUserFail_BlankEmail() throws Exception {
        // given
        final RegisterDto registerDto = RegisterDto.builder()
                .email(null)
                .pwd("password")
                .pwd2("password")
                .uname("")
                .nickname("  ")
                .phoneNumber("1234")
                .build();

        // when
        final ResultActions resultActions = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/user/register")
                                .content(gson.toJson(registerDto))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest())
                .andDo(print()); // 요청 및 응답 로깅
    }

    @DisplayName("회원가입 실패 - 비밀번호 길이 8 미만")
    @Test
    void addUserFail_ShortPassword() throws Exception {
        // given
        final RegisterDto registerDto = RegisterDto.builder()
                .email("mangkyu@naver.com")
                .pwd("pwd")
                .pwd2("pwd")
                .uname("fdgsdfgsd")
                .nickname("dfsdfg")
                .phoneNumber("1234")
                .build();

        // when
        final ResultActions resultActions = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/user/register")
                                .content(gson.toJson(registerDto))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(status().isBadRequest())
                .andDo(print()); // 요청 및 응답 로깅
    }


    @DisplayName("회원가입 성공")
    @Test
    void addUserFail() throws Exception {
        // given
        final RegisterDto registerDto = RegisterDto.builder()
                .email("mangkyu@naver.com")
                .pwd("password")
                .pwd2("password")
                .uname("혜란")
                .nickname("gPfks")
                .phoneNumber("1234")
                .build();

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/user/register")
                        .content(gson.toJson(registerDto))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andDo(print());

    }

    @DisplayName("회원가입 실패 - 비밀번호 불일치")
    @Test
    void passwordMatchValidationFail() throws Exception {
        // given
        final RegisterDto registerDto = RegisterDto.builder()
                .email("mangkyu@naver.com")
                .pwd("password")
                .pwd2("passwore")
                .uname("혜란")
                .nickname("gPfks")
                .phoneNumber("1234")
                .build();

        // when
        final ResultActions resultActions = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/user/register")
                                .content(gson.toJson(registerDto))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andDo(print());

    }

    @DisplayName("회원가입 실패 - 이메일 형식 오류 + 비밀번호 불일치")
    @Test
    void passwordMatchValidationFail2() throws Exception {
        // given
        final RegisterDto registerDto = RegisterDto.builder()
                .email("mangkyu")
                .pwd("password")
                .pwd2("passwore")
                .uname("혜란")
                .nickname("gPfks")
                .phoneNumber("1234")
                .build();

        // when
        final ResultActions resultActions = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/user/register")
                                .content(gson.toJson(registerDto))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andDo(print());

    }

    @DisplayName("회원가입 실패 - 비밀번호 blank + 비밀번호 불일치")
    @Test
    void passwordMatchValidationFail3() throws Exception {
        // given
        final RegisterDto registerDto = RegisterDto.builder()
                .email("mangkyu@naver.com")
                .pwd(" ")
                .pwd2("passwore")
                .uname("혜란")
                .nickname("gPfks")
                .phoneNumber("1234")
                .build();

        // when
        final ResultActions resultActions = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/user/register")
                                .content(gson.toJson(registerDto))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andDo(print());
        // Body = {"registerDto":"비밀번호가 일치하지 않습니다.","pwd":"8자리 이상의 비밀번호를 입력하세요."}

    }



}
