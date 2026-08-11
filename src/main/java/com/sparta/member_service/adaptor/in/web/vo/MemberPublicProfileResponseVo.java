package com.sparta.member_service.adaptor.in.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "회원 공개 프로필 조회 응답")
public class MemberPublicProfileResponseVo {

    @Schema(description = "회원 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String memberUuid;

    @Schema(description = "닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "프로필 이미지 URL")
    private String profileImageUrl;
}
