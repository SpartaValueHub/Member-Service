package com.sparta.member_service.application.exception;

public class MemberNotFoundException extends RuntimeException {

    private final String code;

    public MemberNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
