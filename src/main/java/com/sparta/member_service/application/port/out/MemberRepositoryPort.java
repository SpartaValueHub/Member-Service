package com.sparta.member_service.application.port.out;

import com.sparta.member_service.domain.model.MemberDomain;

import java.util.Optional;

public interface MemberRepositoryPort {

    boolean existsByNickname(String nickname);

    Optional<MemberDomain> findByMemberUuid(String memberUuid);

    MemberDomain save(MemberDomain memberDomain);
}
