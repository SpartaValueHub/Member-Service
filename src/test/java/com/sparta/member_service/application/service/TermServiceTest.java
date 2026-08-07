package com.sparta.member_service.application.service;

import com.sparta.member_service.application.port.in.dto.ActiveTermResultDto;
import com.sparta.member_service.application.port.out.LoadActiveTermsPort;
import com.sparta.member_service.domain.enums.TermCode;
import com.sparta.member_service.domain.enums.TermType;
import com.sparta.member_service.domain.model.TermDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TermServiceTest {

    private static final Instant EFFECTIVE_AT = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    private LoadActiveTermsPort loadActiveTermsPort;

    private TermService termService;

    @BeforeEach
    void setUp() {
        termService = new TermService(loadActiveTermsPort);
    }

    @Test
    void listActiveTerms_returnsMappedResults() {
        TermDomain term = TermDomain.reconstitute(
                1L,
                TermCode.TERMS_OF_SERVICE,
                "이용약관",
                TermType.SERVICE,
                true,
                true,
                "1.0",
                "약관 본문",
                EFFECTIVE_AT,
                null,
                EFFECTIVE_AT,
                EFFECTIVE_AT
        );
        when(loadActiveTermsPort.findAllCurrentlyEffective(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(term));

        List<ActiveTermResultDto> results = termService.listActiveTerms();

        ArgumentCaptor<Instant> atCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(loadActiveTermsPort).findAllCurrentlyEffective(atCaptor.capture());
        assertThat(atCaptor.getValue()).isNotNull();

        assertThat(results).hasSize(1);
        ActiveTermResultDto result = results.get(0);
        assertThat(result.getTermId()).isEqualTo(1L);
        assertThat(result.getTermCode()).isEqualTo(TermCode.TERMS_OF_SERVICE);
        assertThat(result.getTermName()).isEqualTo("이용약관");
        assertThat(result.getTermType()).isEqualTo(TermType.SERVICE);
        assertThat(result.isRequired()).isTrue();
        assertThat(result.getVersion()).isEqualTo("1.0");
        assertThat(result.getContent()).isEqualTo("약관 본문");
        assertThat(result.getEffectiveAt()).isEqualTo(EFFECTIVE_AT);
    }
}
