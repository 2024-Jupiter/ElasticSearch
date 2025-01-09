package com.myfeed.config;

import com.myfeed.response.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.myfeed.response.ErrorCode.*;

@Component
public class CustomSecurityHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        handleException(request, response, LOGIN_REQUIRED);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        handleException(request, response, AUTHENTICATION_REQUIRED);
    }

    private void handleException(HttpServletRequest request, HttpServletResponse response, ErrorCode errorCode) throws IOException, ServletException {
        String url = "/api/users/test";

        request.setAttribute("msg", errorCode);
        request.setAttribute("url", url);
        request.getRequestDispatcher("/api/users/login-error").forward(request, response);
    }
}

