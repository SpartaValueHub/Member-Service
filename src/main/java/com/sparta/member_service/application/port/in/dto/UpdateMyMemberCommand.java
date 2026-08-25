package com.sparta.member_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateMyMemberCommand {

	// 요청 회원 UUID
	private final String memberUuid;
	// 닉네임 (null이면 유지)
	private final String nickname;
	// 프로필 이미지 URL (null이면 유지)
	private final String profileImageUrl;
	// 주소 (null이면 유지)
	private final String address;
}
