package com.sparta.member_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateMemberRequestDto {

    private final String memberUuid;
    private final String nickname;
    private final String profileImageUrl;
    private final String address;
}
