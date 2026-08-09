package com.sparta.member_service.adaptor.out.mysql.repository;

import com.sparta.member_service.adaptor.out.mysql.entity.MemberTermConsentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.member_service.domain.enums.ConsentChannel;

import java.util.List;

public interface MemberTermConsentJpaRepository extends JpaRepository<MemberTermConsentEntity, Long> {

    List<MemberTermConsentEntity> findAllByMemberUuidAndConsentChannel(
            String memberUuid,
            ConsentChannel consentChannel
    );
}
