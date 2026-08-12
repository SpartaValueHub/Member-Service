package com.sparta.member_service.application.port.in.dto;

import com.sparta.member_service.domain.enums.TermCode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TermConsentItemDto {

    private final TermCode termCode;
    private final boolean agreed;
}
