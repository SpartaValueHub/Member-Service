package com.sparta.member_service.application.service;

import com.sparta.member_service.application.exception.DuplicateResourceException;
import com.sparta.member_service.application.exception.InvalidTermConsentException;
import com.sparta.member_service.application.port.in.dto.CreateMemberRequestDto;
import com.sparta.member_service.application.port.in.dto.TermConsentItemDto;
import com.sparta.member_service.application.port.out.LoadActiveTermsPort;
import com.sparta.member_service.application.port.out.LoadMemberTermConsentsPort;
import com.sparta.member_service.application.port.out.MemberRepositoryPort;
import com.sparta.member_service.application.port.out.SaveMemberTermConsentPort;
import com.sparta.member_service.domain.enums.ConsentAction;
import com.sparta.member_service.domain.enums.ConsentChannel;
import com.sparta.member_service.domain.enums.MemberGrade;
import com.sparta.member_service.domain.enums.TermCode;
import com.sparta.member_service.domain.enums.TermType;
import com.sparta.member_service.domain.model.MemberDomain;
import com.sparta.member_service.domain.model.MemberTermConsentDomain;
import com.sparta.member_service.domain.model.TermDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    private static final String MEMBER_UUID = "550e8400-e29b-41d4-a716-446655440000";
    private static final Instant EFFECTIVE_AT = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    private MemberRepositoryPort memberRepositoryPort;

    @Mock
    private LoadActiveTermsPort loadActiveTermsPort;

    @Mock
    private SaveMemberTermConsentPort saveMemberTermConsentPort;

    @Mock
    private LoadMemberTermConsentsPort loadMemberTermConsentsPort;

    @Mock
    private PlatformTransactionManager transactionManager;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());

        memberService = new MemberService(
                memberRepositoryPort,
                loadActiveTermsPort,
                saveMemberTermConsentPort,
                loadMemberTermConsentsPort,
                transactionManager
        );
    }

    @Test
    void createMember_savesNewProfileAndConsents() {
        when(memberRepositoryPort.findByMemberUuid(MEMBER_UUID)).thenReturn(java.util.Optional.empty());
        when(memberRepositoryPort.existsByNickname("닉네임")).thenReturn(false);
        when(loadActiveTermsPort.findAllActive()).thenReturn(activeTerms());
        when(memberRepositoryPort.save(any(MemberDomain.class))).thenAnswer(invocation -> {
            MemberDomain domain = invocation.getArgument(0);
            return MemberDomain.reconstitute(
                    1L,
                    domain.getMemberUuid(),
                    domain.getNickname(),
                    domain.getProfileImageUrl(),
                    domain.getMemberGrade(),
                    domain.getAddress(),
                    domain.isPremium(),
                    domain.isRegions(),
                    null,
                    null
            );
        });

        var result = memberService.createMember(createRequest(
                "  닉네임  ",
                " 서울 ",
                null,
                List.of(
                        consent(TermCode.TERMS_OF_SERVICE, true),
                        consent(TermCode.PRIVACY_POLICY, true),
                        consent(TermCode.EMAIL_MARKETING, true),
                        consent(TermCode.SMS_MARKETING, false)
                )
        ));

        ArgumentCaptor<MemberDomain> memberCaptor = ArgumentCaptor.forClass(MemberDomain.class);
        verify(memberRepositoryPort).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getNickname()).isEqualTo("닉네임");
        assertThat(memberCaptor.getValue().getAddress()).isEqualTo("서울");
        assertThat(result.getMemberUuid()).isEqualTo(MEMBER_UUID);
        assertThat(result.getMemberGrade()).isEqualTo(MemberGrade.BRONZE);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MemberTermConsentDomain>> consentCaptor = ArgumentCaptor.forClass(List.class);
        verify(saveMemberTermConsentPort).saveAll(consentCaptor.capture());
        assertThat(consentCaptor.getValue()).hasSize(3);
        assertThat(consentCaptor.getValue())
                .extracting(MemberTermConsentDomain::getConsentAction)
                .containsOnly(ConsentAction.AGREE);
        assertThat(consentCaptor.getValue())
                .extracting(MemberTermConsentDomain::getConsentChannel)
                .containsOnly(ConsentChannel.SIGN_UP);
    }

    @Test
    void createMember_throwsWhenRequiredConsentMissing() {
        when(loadActiveTermsPort.findAllActive()).thenReturn(activeTerms());

        assertThatThrownBy(() -> memberService.createMember(createRequest(
                "닉네임",
                null,
                null,
                List.of(
                        consent(TermCode.TERMS_OF_SERVICE, true),
                        consent(TermCode.PRIVACY_POLICY, false)
                )
        )))
                .isInstanceOf(InvalidTermConsentException.class)
                .extracting("code")
                .isEqualTo("TERM_REQUIRED_CONSENT_MISSING");

        verify(memberRepositoryPort, never()).save(any());
        verify(saveMemberTermConsentPort, never()).saveAll(any());
    }

    @Test
    void createMember_throwsWhenTermMasterMissing() {
        when(loadActiveTermsPort.findAllActive()).thenReturn(List.of());

        assertThatThrownBy(() -> memberService.createMember(createRequest(
                "닉네임",
                null,
                null,
                List.of(
                        consent(TermCode.TERMS_OF_SERVICE, true),
                        consent(TermCode.PRIVACY_POLICY, true)
                )
        )))
                .isInstanceOf(InvalidTermConsentException.class)
                .extracting("code")
                .isEqualTo("TERM_MASTER_MISSING");

        verify(memberRepositoryPort, never()).save(any());
    }

    @Test
    void createMember_throwsWhenMemberUuidAlreadyExists() {
        when(loadActiveTermsPort.findAllActive()).thenReturn(activeTerms());
        when(memberRepositoryPort.findByMemberUuid(MEMBER_UUID)).thenReturn(java.util.Optional.of(
                MemberDomain.create(MEMBER_UUID, "different", null, null)
        ));

        assertThatThrownBy(() -> memberService.createMember(createRequest("닉네임", null, null, requiredConsents())))
                .isInstanceOf(DuplicateResourceException.class)
                .extracting("code")
                .isEqualTo("MEMBER_PROFILE_CONFLICT");

        verify(memberRepositoryPort, never()).save(any());
    }

    @Test
    void createMember_returnsExistingProfileForIdenticalReplay() {
        MemberDomain existing = MemberDomain.create(MEMBER_UUID, "same-name", null, "Seoul");
        when(memberRepositoryPort.findByMemberUuid(MEMBER_UUID))
                .thenReturn(java.util.Optional.of(existing));
        when(loadActiveTermsPort.findAllActive()).thenReturn(activeTerms());
        when(loadMemberTermConsentsPort.findSignupConsentsByMemberUuid(MEMBER_UUID))
                .thenReturn(requiredConsentDomains());

        var result = memberService.createMember(createRequest("same-name", "Seoul", null, requiredConsents()));

        assertThat(result.isCreated()).isFalse();
        assertThat(result.getMemberUuid()).isEqualTo(MEMBER_UUID);
        assertThat(result.getNickname()).isEqualTo("same-name");
        verify(memberRepositoryPort, never()).save(any());
        verify(saveMemberTermConsentPort, never()).saveAll(any());
    }

    @Test
    void createMember_backfillsMissingSignupConsentsOnReplay() {
        MemberDomain existing = MemberDomain.create(MEMBER_UUID, "same-name", null, "Seoul");
        when(memberRepositoryPort.findByMemberUuid(MEMBER_UUID))
                .thenReturn(java.util.Optional.of(existing));
        when(loadActiveTermsPort.findAllActive()).thenReturn(activeTerms());
        when(loadMemberTermConsentsPort.findSignupConsentsByMemberUuid(MEMBER_UUID))
                .thenReturn(List.of());

        var result = memberService.createMember(createRequest("same-name", "Seoul", null, requiredConsents()));

        assertThat(result.isCreated()).isFalse();
        verify(memberRepositoryPort, never()).save(any());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MemberTermConsentDomain>> consentCaptor = ArgumentCaptor.forClass(List.class);
        verify(saveMemberTermConsentPort).saveAll(consentCaptor.capture());
        assertThat(consentCaptor.getValue()).hasSize(2);
    }

    @Test
    void createMember_backfillsAdditionalOptionalConsentsOnReplay() {
        MemberDomain existing = MemberDomain.create(MEMBER_UUID, "same-name", null, "Seoul");
        when(memberRepositoryPort.findByMemberUuid(MEMBER_UUID))
                .thenReturn(java.util.Optional.of(existing));
        when(loadActiveTermsPort.findAllActive()).thenReturn(activeTerms());
        when(loadMemberTermConsentsPort.findSignupConsentsByMemberUuid(MEMBER_UUID))
                .thenReturn(requiredConsentDomains());

        var result = memberService.createMember(createRequest(
                "same-name",
                "Seoul",
                null,
                List.of(
                        consent(TermCode.TERMS_OF_SERVICE, true),
                        consent(TermCode.PRIVACY_POLICY, true),
                        consent(TermCode.EMAIL_MARKETING, true)
                )
        ));

        assertThat(result.isCreated()).isFalse();
        verify(memberRepositoryPort, never()).save(any());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MemberTermConsentDomain>> consentCaptor = ArgumentCaptor.forClass(List.class);
        verify(saveMemberTermConsentPort).saveAll(consentCaptor.capture());
        assertThat(consentCaptor.getValue())
                .singleElement()
                .extracting(MemberTermConsentDomain::getTermId)
                .isEqualTo(3L);
    }

    @Test
    void createMember_rejectsReplayWhenSignupConsentsConflict() {
        MemberDomain existing = MemberDomain.create(MEMBER_UUID, "same-name", null, "Seoul");
        when(memberRepositoryPort.findByMemberUuid(MEMBER_UUID))
                .thenReturn(java.util.Optional.of(existing));
        when(loadActiveTermsPort.findAllActive()).thenReturn(activeTerms());
        when(loadMemberTermConsentsPort.findSignupConsentsByMemberUuid(MEMBER_UUID))
                .thenReturn(List.of(
                        MemberTermConsentDomain.record(1L, MEMBER_UUID, ConsentAction.AGREE, ConsentChannel.SIGN_UP, EFFECTIVE_AT),
                        MemberTermConsentDomain.record(2L, MEMBER_UUID, ConsentAction.AGREE, ConsentChannel.SIGN_UP, EFFECTIVE_AT),
                        MemberTermConsentDomain.record(3L, MEMBER_UUID, ConsentAction.AGREE, ConsentChannel.SIGN_UP, EFFECTIVE_AT)
                ));

        assertThatThrownBy(() -> memberService.createMember(createRequest(
                "same-name",
                "Seoul",
                null,
                requiredConsents()
        )))
                .isInstanceOf(DuplicateResourceException.class)
                .extracting("code")
                .isEqualTo("MEMBER_PROFILE_CONFLICT");

        verify(memberRepositoryPort, never()).save(any());
        verify(saveMemberTermConsentPort, never()).saveAll(any());
    }

    @Test
    void createMember_throwsWhenNicknameAlreadyExists() {
        when(loadActiveTermsPort.findAllActive()).thenReturn(activeTerms());
        when(memberRepositoryPort.findByMemberUuid(MEMBER_UUID)).thenReturn(java.util.Optional.empty());
        when(memberRepositoryPort.existsByNickname("닉네임")).thenReturn(true);

        assertThatThrownBy(() -> memberService.createMember(createRequest("닉네임", null, null, requiredConsents())))
                .isInstanceOf(DuplicateResourceException.class)
                .extracting("code")
                .isEqualTo("MEMBER_DUPLICATE_NICKNAME");

        verify(memberRepositoryPort, never()).save(any());
    }

    @Test
    void checkNicknameAvailability_returnsTrueWhenNotExists() {
        when(memberRepositoryPort.existsByNickname("닉네임")).thenReturn(false);

        var result = memberService.checkNicknameAvailability("  닉네임  ");

        assertThat(result.isAvailable()).isTrue();
        verify(memberRepositoryPort).existsByNickname("닉네임");
    }

    @Test
    void checkNicknameAvailability_returnsFalseWhenExists() {
        when(memberRepositoryPort.existsByNickname("닉네임")).thenReturn(true);

        var result = memberService.checkNicknameAvailability("닉네임");

        assertThat(result.isAvailable()).isFalse();
    }

    @Test
    void getMyMember_returnsProfile() {
        when(memberRepositoryPort.findByMemberUuid(MEMBER_UUID)).thenReturn(java.util.Optional.of(
                MemberDomain.reconstitute(
                        1L,
                        MEMBER_UUID,
                        "닉네임",
                        null,
                        MemberGrade.BRONZE,
                        "서울",
                        false,
                        false,
                        null,
                        null
                )
        ));

        var result = memberService.getMyMember(MEMBER_UUID);

        assertThat(result.getMemberUuid()).isEqualTo(MEMBER_UUID);
        assertThat(result.getNickname()).isEqualTo("닉네임");
        assertThat(result.getMemberGrade()).isEqualTo(MemberGrade.BRONZE);
        assertThat(result.getAddress()).isEqualTo("서울");
    }

    @Test
    void getMyMember_throwsWhenNotFound() {
        when(memberRepositoryPort.findByMemberUuid(MEMBER_UUID)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> memberService.getMyMember(MEMBER_UUID))
                .isInstanceOf(com.sparta.member_service.application.exception.MemberNotFoundException.class)
                .extracting("code")
                .isEqualTo("MEMBER_NOT_FOUND");
    }

    private CreateMemberRequestDto createRequest(
            String nickname,
            String address,
            String profileImageUrl,
            List<TermConsentItemDto> termConsents
    ) {
        return CreateMemberRequestDto.builder()
                .memberUuid(MEMBER_UUID)
                .nickname(nickname)
                .address(address)
                .profileImageUrl(profileImageUrl)
                .termConsents(termConsents)
                .build();
    }

    private static List<TermConsentItemDto> requiredConsents() {
        return List.of(
                consent(TermCode.TERMS_OF_SERVICE, true),
                consent(TermCode.PRIVACY_POLICY, true)
        );
    }

    private static List<MemberTermConsentDomain> requiredConsentDomains() {
        return List.of(
                MemberTermConsentDomain.record(1L, MEMBER_UUID, ConsentAction.AGREE, ConsentChannel.SIGN_UP, EFFECTIVE_AT),
                MemberTermConsentDomain.record(2L, MEMBER_UUID, ConsentAction.AGREE, ConsentChannel.SIGN_UP, EFFECTIVE_AT)
        );
    }

    private static TermConsentItemDto consent(TermCode termCode, boolean agreed) {
        return TermConsentItemDto.builder()
                .termCode(termCode)
                .agreed(agreed)
                .build();
    }

    private static List<TermDomain> activeTerms() {
        return List.of(
                TermDomain.reconstitute(
                        1L, TermCode.TERMS_OF_SERVICE, "서비스 이용약관", TermType.SERVICE,
                        true, true, "1.0", "content", EFFECTIVE_AT, null, EFFECTIVE_AT, EFFECTIVE_AT
                ),
                TermDomain.reconstitute(
                        2L, TermCode.PRIVACY_POLICY, "개인정보 처리방침", TermType.PRIVACY,
                        true, true, "1.0", "content", EFFECTIVE_AT, null, EFFECTIVE_AT, EFFECTIVE_AT
                ),
                TermDomain.reconstitute(
                        3L, TermCode.EMAIL_MARKETING, "이메일 마케팅", TermType.MARKETING,
                        false, true, "1.0", "content", EFFECTIVE_AT, null, EFFECTIVE_AT, EFFECTIVE_AT
                ),
                TermDomain.reconstitute(
                        4L, TermCode.SMS_MARKETING, "SMS 마케팅", TermType.MARKETING,
                        false, true, "1.0", "content", EFFECTIVE_AT, null, EFFECTIVE_AT, EFFECTIVE_AT
                )
        );
    }
}
