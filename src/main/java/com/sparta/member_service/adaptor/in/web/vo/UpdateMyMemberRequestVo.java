package com.sparta.member_service.adaptor.in.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "내 회원 프로필 수정 요청 (null 필드는 유지)")
public class UpdateMyMemberRequestVo {

	@Schema(description = "닉네임", example = "초량동불주먹")
	private String nickname;

	@Schema(description = "프로필 이미지 CloudFront URL")
	private String profileImageUrl;

	@Schema(description = "주소")
	private String address;
}
