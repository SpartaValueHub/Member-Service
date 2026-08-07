package com.sparta.member_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class CreateMemberRequestDto {

    private final String memberUuid;
    private final String nickname;
    private final String profileImageUrl;
    private final String address;
    private final List<TermConsentItemDto> termConsents;

    public List<TermConsentItemDto> getTermConsents() {
        return termConsents == null ? Collections.emptyList() : termConsents;
    }
}
