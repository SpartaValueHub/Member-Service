package com.sparta.member_service.adaptor.in.web.mapper;

import com.sparta.member_service.adaptor.in.web.vo.ActiveTermResponseVo;
import com.sparta.member_service.application.port.in.dto.ActiveTermResultDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TermWebMapper {

    public List<ActiveTermResponseVo> toVoList(List<ActiveTermResultDto> dtos) {
        return dtos.stream()
                .map(this::toVo)
                .toList();
    }

    public ActiveTermResponseVo toVo(ActiveTermResultDto dto) {
        return ActiveTermResponseVo.builder()
                .termId(dto.getTermId())
                .termCode(dto.getTermCode())
                .termName(dto.getTermName())
                .termType(dto.getTermType())
                .required(dto.isRequired())
                .version(dto.getVersion())
                .content(dto.getContent())
                .effectiveAt(dto.getEffectiveAt())
                .build();
    }
}
