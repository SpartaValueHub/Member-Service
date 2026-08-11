package com.sparta.member_service.application.service;

import com.sparta.member_service.application.exception.DuplicateResourceException;
import com.sparta.member_service.application.exception.InvalidTermConsentException;
import com.sparta.member_service.application.exception.MemberNotFoundException;
import com.sparta.member_service.application.port.in.CheckNicknameAvailabilityUseCase;
import com.sparta.member_service.application.port.in.CreateMemberUseCase;
import com.sparta.member_service.application.port.in.GetMemberPublicProfileUseCase;
import com.sparta.member_service.application.port.in.GetMyMemberUseCase;
import com.sparta.member_service.application.port.in.dto.CreateMemberRequestDto;
import com.sparta.member_service.application.port.in.dto.CreateMemberResultDto;
import com.sparta.member_service.application.port.in.dto.GetMemberPublicProfileResultDto;
import com.sparta.member_service.application.port.in.dto.GetMyMemberResultDto;
import com.sparta.member_service.application.port.in.dto.MemberAvailabilityResultDto;
import com.sparta.member_service.application.port.in.dto.TermConsentItemDto;
import com.sparta.member_service.application.port.out.LoadActiveTermsPort;
import com.sparta.member_service.application.port.out.LoadMemberTermConsentsPort;
import com.sparta.member_service.application.port.out.MemberRepositoryPort;
import com.sparta.member_service.application.port.out.SaveMemberTermConsentPort;
import com.sparta.member_service.domain.enums.ConsentAction;
import com.sparta.member_service.domain.enums.ConsentChannel;
import com.sparta.member_service.domain.enums.TermCode;
import com.sparta.member_service.domain.model.MemberDomain;
import com.sparta.member_service.domain.model.MemberTermConsentDomain;
import com.sparta.member_service.domain.model.TermDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService implements CreateMemberUseCase, CheckNicknameAvailabilityUseCase, GetMyMemberUseCase,
        GetMemberPublicProfileUseCase {

    private final MemberRepositoryPort memberRepositoryPort;
    private final LoadActiveTermsPort loadActiveTermsPort;
    private final SaveMemberTermConsentPort saveMemberTermConsentPort;
    private final LoadMemberTermConsentsPort loadMemberTermConsentsPort;
    private final PlatformTransactionManager transactionManager;

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

        Instant actionAt = Instant.now();
        List<MemberTermConsentDomain> consents = buildSignupConsents(
                memberUuid,
                requestDto.getTermConsents(),
                actionAt
        );

        var existing = memberRepositoryPort.findByMemberUuid(memberUuid);
        if (existing.isPresent()) {
            return idempotentResult(existing.get(), requested, consents);
        }
        if (memberRepositoryPort.existsByNickname(nickname)) {
            throw new DuplicateResourceException("MEMBER_DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다.");
        }

        try {
            return new TransactionTemplate(transactionManager).execute(status -> {
                MemberDomain saved = memberRepositoryPort.save(requested);
                saveMemberTermConsentPort.saveAll(consents);
                return toCreateResult(saved, true);
            });
        } catch (DuplicateResourceException ex) {
            if ("MEMBER_DUPLICATE_UUID".equals(ex.getCode())) {
                MemberDomain concurrent = memberRepositoryPort.findByMemberUuid(memberUuid)
                        .orElseThrow(() -> ex);
                return idempotentResult(concurrent, requested, consents);
            }
            throw ex;
        }
    }

    /**
     * 필수 약관 동의 검증 후 SIGN_UP 채널 AGREE 이력만 생성.
     * 선택 약관은 agreed=true 인 경우만 저장한다.
     */
    private List<MemberTermConsentDomain> buildSignupConsents(
            String memberUuid,
            List<TermConsentItemDto> termConsents,
            Instant actionAt
    ) {
        Map<TermCode, Boolean> agreedByCode = toAgreedMap(termConsents);

        List<TermDomain> consentableTerms = loadActiveTermsPort.findAllActive().stream()
                .filter(term -> term.isConsentableAt(actionAt))
                .toList();

        List<TermDomain> requiredTerms = consentableTerms.stream()
                .filter(TermDomain::isRequired)
                .toList();
        if (requiredTerms.isEmpty()) {
            throw new InvalidTermConsentException(
                    "TERM_MASTER_MISSING",
                    "필수 약관 마스터 데이터가 없습니다. 약관을 등록한 뒤 다시 시도해 주세요."
            );
        }

        for (TermDomain required : requiredTerms) {
            if (!Boolean.TRUE.equals(agreedByCode.get(required.getTermCode()))) {
                throw new InvalidTermConsentException(
                        "TERM_REQUIRED_CONSENT_MISSING",
                        "필수 약관에 동의해 주세요."
                );
            }
        }

        List<MemberTermConsentDomain> consents = new ArrayList<>();
        for (TermDomain term : consentableTerms) {
            if (!Boolean.TRUE.equals(agreedByCode.get(term.getTermCode()))) {
                continue;
            }
            consents.add(MemberTermConsentDomain.record(
                    term,
                    memberUuid,
                    ConsentAction.AGREE,
                    ConsentChannel.SIGN_UP,
                    actionAt
            ));
        }
        return consents;
    }

    private Map<TermCode, Boolean> toAgreedMap(List<TermConsentItemDto> termConsents) {
        Map<TermCode, Boolean> agreedByCode = new EnumMap<>(TermCode.class);
        if (termConsents == null) {
            return agreedByCode;
        }
        for (TermConsentItemDto item : termConsents) {
            if (item == null || item.getTermCode() == null) {
                throw new IllegalArgumentException("termConsents.termCode는 필수입니다.");
            }
            agreedByCode.put(item.getTermCode(), item.isAgreed());
        }
        return agreedByCode;
    }

    /**
     * 동일 memberUuid 재요청 시 프로필·약관 동의를 맞춘다.
     * 프로필이 같고 SIGN_UP 동의 이력만 비어 있거나 부분 누락이면 누락분을 저장한다.
     */
    private CreateMemberResultDto idempotentResult(
            MemberDomain existing,
            MemberDomain requested,
            List<MemberTermConsentDomain> requestedConsents
    ) {
        if (!Objects.equals(existing.getNickname(), requested.getNickname())
                || !Objects.equals(existing.getProfileImageUrl(), requested.getProfileImageUrl())
                || !Objects.equals(existing.getAddress(), requested.getAddress())) {
            throw new DuplicateResourceException("MEMBER_PROFILE_CONFLICT", "이미 다른 정보로 등록된 회원입니다.");
        }

        Set<Long> existingTermIds = loadMemberTermConsentsPort.findSignupConsentsByMemberUuid(existing.getMemberUuid())
                .stream()
                .map(MemberTermConsentDomain::getTermId)
                .collect(Collectors.toSet());
        Set<Long> requestedTermIds = requestedConsents.stream()
                .map(MemberTermConsentDomain::getTermId)
                .collect(Collectors.toSet());

        if (existingTermIds.equals(requestedTermIds)) {
            return toCreateResult(existing, false);
        }
        if (!requestedTermIds.containsAll(existingTermIds)) {
            throw new DuplicateResourceException("MEMBER_PROFILE_CONFLICT", "이미 다른 정보로 등록된 회원입니다.");
        }

        List<MemberTermConsentDomain> missingConsents = requestedConsents.stream()
                .filter(consent -> !existingTermIds.contains(consent.getTermId()))
                .toList();
        if (!missingConsents.isEmpty()) {
            new TransactionTemplate(transactionManager).execute(status -> {
                saveMemberTermConsentPort.saveAll(missingConsents);
                return null;
            });
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

    @Override
    public GetMemberPublicProfileResultDto getMemberPublicProfile(String memberUuid) {
        String normalizedMemberUuid = memberUuid == null ? "" : memberUuid.trim();
        if (normalizedMemberUuid.isBlank()) {
            throw new IllegalArgumentException("memberUuid는 필수입니다.");
        }

        MemberDomain member = memberRepositoryPort.findByMemberUuid(normalizedMemberUuid)
                .orElseThrow(() -> new MemberNotFoundException("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."));

        return GetMemberPublicProfileResultDto.builder()
                .memberUuid(member.getMemberUuid())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }
}
