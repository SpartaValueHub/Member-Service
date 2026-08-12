package com.sparta.member_service.adaptor.in.web;

import com.sparta.member_service.adaptor.in.web.mapper.TermWebMapper;
import com.sparta.member_service.adaptor.in.web.vo.ActiveTermResponseVo;
import com.sparta.member_service.application.port.in.ListActiveTermsUseCase;
import com.sparta.member_service.application.port.in.dto.ActiveTermResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Term", description = "약관 API")
@RequestMapping("/api/v1/terms")
@RestController
@RequiredArgsConstructor
public class TermController {

    private final ListActiveTermsUseCase listActiveTermsUseCase;
    private final TermWebMapper termWebMapper;

    @Operation(summary = "현재 유효 약관 목록", description = "회원가입 등에 노출할 현재 유효한 약관 목록을 조회합니다.")
    @GetMapping("/active")
    public List<ActiveTermResponseVo> listActiveTerms() {
        List<ActiveTermResultDto> resultDtos = listActiveTermsUseCase.listActiveTerms();
        return termWebMapper.toVoList(resultDtos);
    }
}
