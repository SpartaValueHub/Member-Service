package com.sparta.member_service.adaptor.out.mysql;

import com.sparta.member_service.adaptor.out.mysql.mapper.MemberTermConsentEntityMapper;
import com.sparta.member_service.adaptor.out.mysql.repository.MemberTermConsentJpaRepository;
import com.sparta.member_service.application.port.out.SaveMemberTermConsentPort;
import com.sparta.member_service.domain.model.MemberTermConsentDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberTermConsentRepositoryAdapter implements SaveMemberTermConsentPort {

    private final MemberTermConsentJpaRepository memberTermConsentJpaRepository;
    private final MemberTermConsentEntityMapper memberTermConsentEntityMapper;

    @Override
    public void saveAll(List<MemberTermConsentDomain> consents) {
        if (consents == null || consents.isEmpty()) {
            return;
        }
        memberTermConsentJpaRepository.saveAll(
                consents.stream()
                        .map(memberTermConsentEntityMapper::toEntity)
                        .toList()
        );
    }
}
