package com.sparta.member_service.adaptor.out.mysql;

import com.sparta.member_service.adaptor.out.mysql.entity.MemberEntity;
import com.sparta.member_service.adaptor.out.mysql.mapper.MemberEntityMapper;
import com.sparta.member_service.adaptor.out.mysql.repository.MemberJpaRepository;
import com.sparta.member_service.application.exception.DuplicateResourceException;
import com.sparta.member_service.application.exception.MemberNotFoundException;
import com.sparta.member_service.application.port.out.MemberRepositoryPort;
import com.sparta.member_service.domain.model.MemberDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberRepositoryAdapter implements MemberRepositoryPort {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberEntityMapper memberEntityMapper;

    @Override
    public boolean existsByNickname(String nickname) {
        return memberJpaRepository.existsByNickname(nickname);
    }

    @Override
    public Optional<MemberDomain> findByMemberUuid(String memberUuid) {
        return memberJpaRepository.findByMemberUuid(memberUuid)
                .map(memberEntityMapper::toDomain);
    }

    @Override
    public MemberDomain save(MemberDomain memberDomain) {
        try {
            // PK가 있으면 기존 행 갱신, 없으면 신규 insert
            if (memberDomain.getMemberId() != null) {
                MemberEntity entity = memberJpaRepository.findById(memberDomain.getMemberId())
                        .orElseThrow(() -> new MemberNotFoundException(
                                "MEMBER_NOT_FOUND",
                                "회원을 찾을 수 없습니다."
                        ));
                memberEntityMapper.updateEntity(entity, memberDomain);
                return memberEntityMapper.toDomain(memberJpaRepository.saveAndFlush(entity));
            }
            MemberEntity saved = memberJpaRepository.saveAndFlush(memberEntityMapper.toEntity(memberDomain));
            return memberEntityMapper.toDomain(saved);
        } catch (DataIntegrityViolationException ex) {
            throw MemberDataIntegrityViolationMapper.mapDuplicate(ex)
                    .map(runtimeException -> (RuntimeException) runtimeException)
                    .orElse(ex);
        }
    }
}
