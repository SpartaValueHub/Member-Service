package com.sparta.member_service.adaptor.in.web.vo;

import com.sparta.member_service.domain.enums.TermCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "약관 동의 항목")
public class TermConsentItemVo {

    @Schema(description = "약관 코드", example = "TERMS_OF_SERVICE")
    private TermCode termCode;

    @Schema(description = "동의 여부", example = "true")
    private boolean agreed;
}
