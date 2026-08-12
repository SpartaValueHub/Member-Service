package com.sparta.member_service.adaptor.out.mysql.repository;

import com.sparta.member_service.adaptor.out.mysql.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {

    boolean existsByNickname(String nickname);

    Optional<MemberEntity> findByMemberUuid(String memberUuid);
}
