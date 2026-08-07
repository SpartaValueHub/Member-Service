package com.sparta.member_service.adaptor.out.mysql.repository;

import com.sparta.member_service.adaptor.out.mysql.entity.MemberTermConsentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTermConsentJpaRepository extends JpaRepository<MemberTermConsentEntity, Long> {
}
