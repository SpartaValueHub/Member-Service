package com.sparta.member_service.application.port.in.dto;

import com.sparta.member_service.domain.enums.TermCode;
import com.sparta.member_service.domain.enums.TermType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ActiveTermResultDto {

    private final Long termId;
    private final TermCode termCode;
    private final String termName;
    private final TermType termType;
    private final boolean required;
    private final String version;
    private final String content;
    private final Instant effectiveAt;
}
