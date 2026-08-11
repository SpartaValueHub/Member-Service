package com.sparta.member_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetMemberPublicProfileResultDto {

    private final String memberUuid;
    private final String nickname;
    private final String profileImageUrl;
}
