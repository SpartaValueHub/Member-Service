package com.sparta.member_service.adaptor.in.web.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ErrorResponseVo {

    private final Instant timestamp;
    private final int status;
    private final String code;
    private final String message;
    private final String path;
    private final List<FieldErrorVo> fieldErrors;

    @Getter
    @Builder
    public static class FieldErrorVo {
        private final String field;
        private final String code;
        private final String message;
    }
}
