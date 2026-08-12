package com.sparta.member_service.adaptor.in.web.vo;

import com.sparta.member_service.domain.enums.MemberGrade;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "내 회원 프로필 조회 응답")
public class MemberProfileResponseVo {

    @Schema(description = "회원 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String memberUuid;

    @Schema(description = "닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "프로필 이미지 URL")
    private String profileImageUrl;

    @Schema(description = "회원 등급", example = "BRONZE")
    private MemberGrade memberGrade;

    @Schema(description = "주소")
    private String address;
}
