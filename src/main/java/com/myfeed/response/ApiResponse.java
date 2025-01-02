package com.myfeed.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // json 직렬화 시 null 값을 직렬화하는 과정에서 오류 발생 -> null 값을 제외하도록 설정
public class ApiResponse<T> {

	private final String status;

	private final T data;

	private final String errorCode;

	// private String errorMessage;

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>("success", data, null);
	}

	public static <T> ApiResponse<?> error(String errorCode) {
		return new ApiResponse<>("error", null, errorCode);
	}
}
