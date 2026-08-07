package com.sparta.member_service.application.port.in.dto;

import com.sparta.member_service.domain.enums.MemberGrade;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateMemberResultDto {

    private final boolean created;
    private final String memberUuid;
    private final String nickname;
    private final String profileImageUrl;
    private final MemberGrade memberGrade;
    private final String address;
}
