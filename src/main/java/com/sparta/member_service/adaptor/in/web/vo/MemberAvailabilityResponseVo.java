package com.sparta.member_service.adaptor.in.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "닉네임 중복 확인 결과")
public class MemberAvailabilityResponseVo {

    @Schema(description = "사용 가능 여부")
    private final boolean available;
}
