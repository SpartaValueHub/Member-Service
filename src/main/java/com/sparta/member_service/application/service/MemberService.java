package com.sparta.member_service.application.service;

import com.sparta.member_service.application.exception.DuplicateResourceException;
import com.sparta.member_service.application.exception.MemberNotFoundException;
import com.sparta.member_service.application.port.in.CheckNicknameAvailabilityUseCase;
import com.sparta.member_service.application.port.in.CreateMemberUseCase;
import com.sparta.member_service.application.port.in.GetMyMemberUseCase;
import com.sparta.member_service.application.port.in.dto.CreateMemberRequestDto;
import com.sparta.member_service.application.port.in.dto.CreateMemberResultDto;
import com.sparta.member_service.application.port.in.dto.GetMyMemberResultDto;
import com.sparta.member_service.application.port.in.dto.MemberAvailabilityResultDto;
import com.sparta.member_service.application.port.out.MemberRepositoryPort;
import com.sparta.member_service.domain.model.MemberDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService implements CreateMemberUseCase, CheckNicknameAvailabilityUseCase, GetMyMemberUseCase {

    private final MemberRepositoryPort memberRepositoryPort;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CreateMemberResultDto createMember(CreateMemberRequestDto requestDto) {
        String memberUuid = requestDto.getMemberUuid() == null ? "" : requestDto.getMemberUuid().trim();
        String nickname = MemberDomain.normalizeNicknameForLookup(requestDto.getNickname());

        MemberDomain requested = MemberDomain.create(
                memberUuid,
                nickname,
                requestDto.getProfileImageUrl(),
                requestDto.getAddress()
        );

        var existing = memberRepositoryPort.findByMemberUuid(memberUuid);
        if (existing.isPresent()) {
            return idempotentResult(existing.get(), requested);
        }
        if (memberRepositoryPort.existsByNickname(nickname)) {
            throw new DuplicateResourceException("MEMBER_DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다.");
        }

        MemberDomain saved;
        try {
            saved = memberRepositoryPort.save(requested);
        } catch (DuplicateResourceException ex) {
            if ("MEMBER_DUPLICATE_UUID".equals(ex.getCode())) {
                MemberDomain concurrent = memberRepositoryPort.findByMemberUuid(memberUuid)
                        .orElseThrow(() -> ex);
                return idempotentResult(concurrent, requested);
            }
            throw ex;
        }

        return toCreateResult(saved, true);
    }

    private CreateMemberResultDto idempotentResult(MemberDomain existing, MemberDomain requested) {
        if (!Objects.equals(existing.getNickname(), requested.getNickname())
                || !Objects.equals(existing.getProfileImageUrl(), requested.getProfileImageUrl())
                || !Objects.equals(existing.getAddress(), requested.getAddress())) {
            throw new DuplicateResourceException("MEMBER_PROFILE_CONFLICT", "이미 다른 정보로 등록된 회원입니다.");
        }
        return toCreateResult(existing, false);
    }

    private CreateMemberResultDto toCreateResult(MemberDomain member, boolean created) {
        return CreateMemberResultDto.builder()
                .created(created)
                .memberUuid(member.getMemberUuid())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .memberGrade(member.getMemberGrade())
                .address(member.getAddress())
                .build();
    }

    @Override
    public MemberAvailabilityResultDto checkNicknameAvailability(String nickname) {
        String normalized = MemberDomain.normalizeNicknameForLookup(nickname);
        return MemberAvailabilityResultDto.builder()
                .available(!memberRepositoryPort.existsByNickname(normalized))
                .build();
    }

    @Override
    public GetMyMemberResultDto getMyMember(String memberUuid) {
        String normalizedMemberUuid = memberUuid == null ? "" : memberUuid.trim();
        if (normalizedMemberUuid.isBlank()) {
            throw new MemberNotFoundException("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다.");
        }

        MemberDomain member = memberRepositoryPort.findByMemberUuid(normalizedMemberUuid)
                .orElseThrow(() -> new MemberNotFoundException("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."));

        return GetMyMemberResultDto.builder()
                .memberUuid(member.getMemberUuid())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .memberGrade(member.getMemberGrade())
                .address(member.getAddress())
                .build();
    }
}
