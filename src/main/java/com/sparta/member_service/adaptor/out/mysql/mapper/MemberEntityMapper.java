package com.sparta.member_service.adaptor.out.mysql.mapper;

import com.sparta.member_service.adaptor.out.mysql.entity.MemberEntity;
import com.sparta.member_service.domain.model.MemberDomain;
import org.springframework.stereotype.Component;

/** memberUuid는 updateEntity에서 제외 — 신규 생성 시 toEntity만 사용 */
@Component
public class MemberEntityMapper {

    public MemberEntity toEntity(MemberDomain domain) {
        return MemberEntity.builder()
                .memberUuid(domain.getMemberUuid())
                .nickname(domain.getNickname())
                .profileImageUrl(domain.getProfileImageUrl())
                .memberGrade(domain.getMemberGrade())
                .address(domain.getAddress())
                .isPremium(domain.isPremium())
                .isRegions(domain.isRegions())
                .build();
    }

    public void updateEntity(MemberEntity entity, MemberDomain domain) {
        entity.updateProfile(
                domain.getNickname(),
                domain.getProfileImageUrl(),
                domain.getMemberGrade(),
                domain.getAddress(),
                domain.isPremium()
        );
    }

    public MemberDomain toDomain(MemberEntity entity) {
        return MemberDomain.reconstitute(
                entity.getMemberId(),
                entity.getMemberUuid(),
                entity.getNickname(),
                entity.getProfileImageUrl(),
                entity.getMemberGrade(),
                entity.getAddress(),
                entity.isPremium(),
                entity.isRegions(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
