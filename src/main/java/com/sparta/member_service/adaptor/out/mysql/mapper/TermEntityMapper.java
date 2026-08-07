package com.sparta.member_service.adaptor.out.mysql.mapper;

import com.sparta.member_service.adaptor.out.mysql.entity.TermEntity;
import com.sparta.member_service.domain.model.TermDomain;
import org.springframework.stereotype.Component;

/** termCode는 updateEntity에서 제외 */
@Component
public class TermEntityMapper {

    public TermEntity toEntity(TermDomain domain) {
        return TermEntity.builder()
                .termCode(domain.getTermCode())
                .termName(domain.getTermName())
                .termType(domain.getTermType())
                .isRequired(domain.isRequired())
                .isActive(domain.isActive())
                .version(domain.getVersion())
                .content(domain.getContent())
                .effectiveAt(domain.getEffectiveAt())
                .expiredAt(domain.getExpiredAt())
                .build();
    }

    public void updateEntity(TermEntity entity, TermDomain domain) {
        entity.updateTerm(
                domain.getTermName(),
                domain.getTermType(),
                domain.isRequired(),
                domain.isActive(),
                domain.getVersion(),
                domain.getContent(),
                domain.getEffectiveAt(),
                domain.getExpiredAt()
        );
    }

    public TermDomain toDomain(TermEntity entity) {
        return TermDomain.reconstitute(
                entity.getTermId(),
                entity.getTermCode(),
                entity.getTermName(),
                entity.getTermType(),
                entity.isRequired(),
                entity.isActive(),
                entity.getVersion(),
                entity.getContent(),
                entity.getEffectiveAt(),
                entity.getExpiredAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
