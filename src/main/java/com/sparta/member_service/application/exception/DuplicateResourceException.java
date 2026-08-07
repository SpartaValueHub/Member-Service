package com.sparta.member_service.application.exception;

/** memberUuid·nickname 중복 — code로 필드 구분(MEMBER_DUPLICATE_*) */
public class DuplicateResourceException extends RuntimeException {

    private final String code;

    public DuplicateResourceException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
