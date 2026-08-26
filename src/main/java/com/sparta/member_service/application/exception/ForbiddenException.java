package com.sparta.member_service.application.exception;

// 권한 없음 (타인 미디어 key 등)
public class ForbiddenException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public ForbiddenException(String code, String message) {
		super(message);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
