package com.sparta.member_service.adaptor.out.mysql.mapper;

import com.sparta.member_service.adaptor.out.mysql.entity.MemberTermConsentEntity;
import com.sparta.member_service.domain.model.MemberTermConsentDomain;
import org.springframework.stereotype.Component;

/** 동의 이력은 append-only — updateEntity 미지원 */
@Component
public class MemberTermConsentEntityMapper {

    public MemberTermConsentEntity toEntity(MemberTermConsentDomain domain) {
        return MemberTermConsentEntity.builder()
                .termId(domain.getTermId())
                .memberUuid(domain.getMemberUuid())
                .consentAction(domain.getConsentAction())
                .actionAt(domain.getActionAt())
                .consentChannel(domain.getConsentChannel())
                .build();
    }

    public MemberTermConsentDomain toDomain(MemberTermConsentEntity entity) {
        return MemberTermConsentDomain.reconstitute(
                entity.getMemberTermConsentId(),
                entity.getTermId(),
                entity.getMemberUuid(),
                entity.getConsentAction(),
                entity.getActionAt(),
                entity.getConsentChannel(),
                entity.getCreatedAt()
        );
    }
}
