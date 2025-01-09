package com.myfeed.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	/**
	 * 사용자
	 */
	// 로그인
	BAD_CREDENTIALS("BAD_CREDENTIALS", "아이디 또는 비밀번호가 틀렸습니다."),
	USER_DISABLED("USER_DISABLED", "비활성화된 회원입니다. 관리자에게 문의하세요"),
	LOGIN_ERROR("LOGIN_ERROR", "로그인 중 오류가 발생했습니다. 다시 시도해주세요."),

	//회원가입
	ID_ALREADY_EXIST("INCLUDED_BLOCK_REPLY", "이미 사용 중인 이메일입니다."),
	NICKNAME_ALREADY_EXIST("NICKNAME_ALREADY_EXIST", "이미 사용 중인 닉네임입니다."),

	//아이디, 비밀번호 찾기
	USER_NOT_FOUND("USER_NOT_FOUND","사용자를 찾을 수 없습니다."),
	ID_CONFLICT("ID_CONFLICT", "소셜 로그인으로 시도하세요."),
	PROFILE_PHONE_MISMATCH("PROFILE_PHONE_MISMATCH", "휴대폰 번호가 기존 정보와 일치하지 않습니다."),

	// 인증 되지 않은 사용자
	AUTHENTICATION_REQUIRED("AUTHENTICATION_REQUIRED", "인증되지 않은 사용자입니다."),

	/**
	 * 게시글 Es
	 */
	POST_ES_NOT_FOUND("POST_ES_NOT_FOUND", "PostEs를 찾을 수 없습니다."),

	/**
	 * 게시글
	 */
	POST_NOT_FOUND("POST_NOT_FOUND", "게시글을 찾을 수 없습니다."),
	ACCESS_DENIED("ACCESS_DENIED", "이 카테고리는 관리자만 선택 가능 합니다. "),

	// 이미지
	IMAGE_UPLOAD_FAIL("IMAGE_UPLOAD_FAIL", "이미지 업로드 실패 했습니다."),
	WRONG_IMAGE_FILE("WRONG_IMAGE_FILE", "잘못된 이미지 파일 형식 입니다."),

	USER_DELETED("USER_DELETED", "삭제된 사용자 입니다."),
	INCLUDED_DELETED_USER_IN_POST("INCLUDED_DELETED_USER_IN_POST", "삭제된 사용자의 게시글이 포함 되어 있습니다."),
	INCLUDED_BLOCK_POST("INCLUDED_BLOCK_POST", "차단된 게시글이 포함 되어 있습니다."),

	/**
	 * 댓글
	 */
	REPLY_NOT_FOUND("REPLY_NOT_FOUND", "댓글을 찾을 수 없습니다."),
	INCLUDED_DELETED_USER_IN_REPLY("INCLUDED_DELETED_USER_IN_REPLY", "삭제된 사용자의 댓글이 포함 되어 있습니다."),
	INCLUDED_BLOCK_REPLY("INCLUDED_BLOCK_REPLY", "차단된 댓글이 포함 되어 있습니다."),

	/**
	 * 신고
	 */
	REPORT_NOT_FOUND("REPORT_NOT_FOUND", "신고를 찾을 수 없습니다."),
	NOT_REPORTED("NOT_REPORTED", "신고 되지 않은 상태 입니다."),
	INCLUDED_DELETED_USER_POST_IN_REPORT("INCLUDED_DELETED_USER_POST_IN_REPORT", "삭제된 사용자의 게시글이 신고 리스트에 포함 되어 있습니다."),
	INCLUDED_DELETED_USER_REPLY_IN_REPORT("INCLUDED_DELETED_USER_REPLY_IN_REPORT", "삭제된 사용자의 댓글이 신고 리스트에 포함 되어 있습니다."),
	POST_BLOCKED("POST_BLOCKED", "차단된 게시글 입니다."),
	REPLY_BLOCKED("REPLY_BLOCKED", "차단된 댓글 입니다."),
	ALREADY_BLOCKED_POST("ALREADY_BLOCKED_POST", "이미 차단된 게시글 입니다."),
	ALREADY_BLOCKED_REPLY("ALREADY_BLOCKED_REPLY", "이미 차단된 댓글 입니다."),
	ALREADY_UNBLOCKED_POST("ALREADY_BLOCKED_POST", "이미 차단 해제된 게시글 입니다."),
	ALREADY_UNBLOCKED_REPLY("ALREADY_BLOCKED_REPLY", "이미 차단 해제된 댓글 입니다."),
	POST_UNBLOCKED("POST_UNBLOCKED", "차단 되지 않은 게시글 입니다."),
	REPLY_UNBLOCKED("REPLY_UNBLOCKED", "차단된 되지 않은 댓글 입니다."),
	REPORT_COMPLETED("REPORT_COMPLETED", "신고 처리 완료 상태 입니다."),
	REPORT_PENDING("REPORT_PENDING", "신고 처리 대기 상태 중 입니다."),
	CAN_NOT_REPORT_DELETED_USER_POST("CAN_NOT_REPORT_DELETED_USER_POST", "삭제된 사용자는 게시글을 신고할 수 없습니다."),
	CAN_NOT_REPORT_DELETED_USER_REPLY("CAN_NOT_REPORT_DELETED_USER_REPLY", "삭제된 사용자는 댓글을 신고할 수 없습니다.");


	private final String errorCode;
	private final String errorMessage;
}