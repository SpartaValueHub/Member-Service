package com.sparta.member_service.adaptor.in.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "회원 프로필 생성 요청")
public class CreateMemberRequestVo {

    @Schema(description = "회원 UUID (auth-service authUuid)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String memberUuid;

    @Schema(description = "닉네임", example = "홍급동")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.png")
    private String profileImageUrl;

    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "회원가입 약관 동의 목록")
    private List<TermConsentItemVo> termConsents = new ArrayList<>();
}
