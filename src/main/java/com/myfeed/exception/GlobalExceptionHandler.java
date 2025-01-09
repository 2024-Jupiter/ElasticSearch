package com.myfeed.exception;

import java.util.HashMap;
import java.util.Map;
import jdk.swing.interop.SwingInterOpUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.myfeed.response.ErrorCode;
import com.myfeed.response.ErrorResponse;
import org.springframework.web.bind.annotation.ResponseBody;
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

        // ErrorResponse errorResponse = new ErrorResponse("VALIDATION_FAIL", "유효성 검증에 실패했습니다.");;
        //
        // // {registerDto = 비밀번호가 일치하지 않습니다., email = 이메일 형식이 올바르지 않습니다.}
        // for (Map.Entry<String, String> entry : errors.entrySet()) {
        //     String fieldName = entry.getKey();
        //
        //     if (fieldName.equals("registerDto")) {
        //         errorResponse = ErrorResponse.of(fieldName);
        //         break;
        //     } else if (fieldName.equals("email")) {
        //         errorResponse = new ErrorResponse("INVALID_EMAIL_TYPE", errorMessage);
        //         break;
        //     }
        // }

        return new ErrorResponse("VALIDATION_FAIL", "유효성 검증에 실패했습니다.");
    }

//     @ExceptionHandler(UserNotFoundException.class)
//     public ErrorResponse handleUserNotFoundException(UserNotFoundException ex) {
// //        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
// //                Map.of(
// //                        "error", "User Not Found",
// //                        "message", ex.getMessage(),
// //                        "status", HttpStatus.NOT_FOUND.value()
// //                )
// //        );
//
//         return new ErrorResponse(HttpStatus.NOT_FOUND.toString());
//     }

    // @ExceptionHandler(CustomException.class)
    // public ErrorResponse handleCustomException(CustomException ex) {
    //     String message = ex.getMessage();
    //     String errorCode = ex.getErrorCode();
    //
    //     return new ErrorResponse(errorCode);
    // }

}


