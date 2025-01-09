package com.myfeed.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.myfeed.response.ErrorCode;
import com.myfeed.response.ErrorResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//@ControllerAdvice
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExpectedException.class)
    protected ErrorResponse handleExpectedException(final ExpectedException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return new ErrorResponse(errorCode.getErrorCode(), errorCode.getErrorMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = (error instanceof org.springframework.validation.FieldError)
                    ? ((org.springframework.validation.FieldError) error).getField()
                    : error.getObjectName();
            errors.put(fieldName, error.getDefaultMessage());
        });
        System.out.println(errors);
        // {registerDto=비밀번호가 일치하지 않습니다., email=이메일 형식이 올바르지 않습니다.}
        return new ErrorResponse("VALIDATION_FAIL", "유효성 검증에 실패했습니다.");
    }
}


