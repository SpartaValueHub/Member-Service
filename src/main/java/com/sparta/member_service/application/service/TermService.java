package com.sparta.member_service.application.service;

import com.sparta.member_service.application.port.in.ListActiveTermsUseCase;
import com.sparta.member_service.application.port.in.dto.ActiveTermResultDto;
import com.sparta.member_service.application.port.out.LoadActiveTermsPort;
import com.sparta.member_service.domain.model.TermDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermService implements ListActiveTermsUseCase {

    private final LoadActiveTermsPort loadActiveTermsPort;

    @Override
    public List<ActiveTermResultDto> listActiveTerms() {
        Instant now = Instant.now();
        return loadActiveTermsPort.findAllCurrentlyEffective(now).stream()
                .map(this::toResultDto)
                .toList();
    }

    private ActiveTermResultDto toResultDto(TermDomain term) {
        return ActiveTermResultDto.builder()
                .termId(term.getTermId())
                .termCode(term.getTermCode())
                .termName(term.getTermName())
                .termType(term.getTermType())
                .required(term.isRequired())
                .version(term.getVersion())
                .content(term.getContent())
                .effectiveAt(term.getEffectiveAt())
                .build();
    }
}
