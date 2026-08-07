package com.sparta.member_service.adaptor.in.web.vo;

import com.sparta.member_service.domain.enums.TermCode;
import com.sparta.member_service.domain.enums.TermType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@Schema(description = "현재 유효한 약관")
public class ActiveTermResponseVo {

    @Schema(description = "약관 ID")
    private final Long termId;

    @Schema(description = "약관 코드")
    private final TermCode termCode;

    @Schema(description = "약관명")
    private final String termName;

    @Schema(description = "약관 유형")
    private final TermType termType;

    @Schema(description = "필수 여부")
    private final boolean required;

    @Schema(description = "약관 버전")
    private final String version;

    @Schema(description = "약관 본문")
    private final String content;

    @Schema(description = "시행 일시 (ISO-8601)")
    private final Instant effectiveAt;
}
