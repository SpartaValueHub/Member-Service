package com.sparta.member_service.adaptor.out.mysql;

import com.sparta.member_service.adaptor.out.mysql.mapper.TermEntityMapper;
import com.sparta.member_service.adaptor.out.mysql.repository.TermJpaRepository;
import com.sparta.member_service.application.port.out.LoadActiveTermsPort;
import com.sparta.member_service.domain.model.TermDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TermRepositoryAdapter implements LoadActiveTermsPort {

    private final TermJpaRepository termJpaRepository;
    private final TermEntityMapper termEntityMapper;

    @Override
    public List<TermDomain> findAllActive() {
        return termJpaRepository.findByIsActiveTrue().stream()
                .map(termEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<TermDomain> findAllCurrentlyEffective(Instant at) {
        return termJpaRepository.findCurrentlyEffectiveTerms(at).stream()
                .map(termEntityMapper::toDomain)
                .toList();
    }
}
