package com.sparta.member_service.application.exception;

/** 회원가입 약관 동의 검증 실패 — code로 원인 구분 */
public class InvalidTermConsentException extends RuntimeException {

    private final String code;

    public InvalidTermConsentException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
