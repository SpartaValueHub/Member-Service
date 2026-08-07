package com.sparta.member_service.application.service;

import com.sparta.member_service.application.exception.DuplicateResourceException;
import com.sparta.member_service.application.port.in.dto.CreateMemberRequestDto;
import com.sparta.member_service.application.port.out.MemberRepositoryPort;
import com.sparta.member_service.domain.enums.MemberGrade;
import com.sparta.member_service.domain.model.MemberDomain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    private static final String MEMBER_UUID = "550e8400-e29b-41d4-a716-446655440000";

    @Mock
    private MemberRepositoryPort memberRepositoryPort;

    @InjectMocks
    private MemberService memberService;

    @Test
    void createMember_savesNewProfile() {
        when(memberRepositoryPort.findByMemberUuid(MEMBER_UUID)).thenReturn(java.util.Optional.empty());
        when(memberRepositoryPort.existsByNickname("닉네임")).thenReturn(false);
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

        var result = memberService.createMember(createRequest("  닉네임  ", " 서울 ", null));

        ArgumentCaptor<MemberDomain> captor = ArgumentCaptor.forClass(MemberDomain.class);
        verify(memberRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getNickname()).isEqualTo("닉네임");
        assertThat(captor.getValue().getAddress()).isEqualTo("서울");
        assertThat(result.getMemberUuid()).isEqualTo(MEMBER_UUID);
        assertThat(result.getMemberGrade()).isEqualTo(MemberGrade.BRONZE);
    }

    @Test
    void createMember_throwsWhenMemberUuidAlreadyExists() {
        when(memberRepositoryPort.findByMemberUuid(MEMBER_UUID)).thenReturn(java.util.Optional.of(
                MemberDomain.create(MEMBER_UUID, "different", null, null)
        ));

        assertThatThrownBy(() -> memberService.createMember(createRequest("닉네임", null, null)))
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

        var result = memberService.createMember(createRequest("same-name", "Seoul", null));

        assertThat(result.isCreated()).isFalse();
        assertThat(result.getMemberUuid()).isEqualTo(MEMBER_UUID);
        assertThat(result.getNickname()).isEqualTo("same-name");
        verify(memberRepositoryPort, never()).save(any());
    }

    @Test
    void createMember_throwsWhenNicknameAlreadyExists() {
        when(memberRepositoryPort.findByMemberUuid(MEMBER_UUID)).thenReturn(java.util.Optional.empty());
        when(memberRepositoryPort.existsByNickname("닉네임")).thenReturn(true);

        assertThatThrownBy(() -> memberService.createMember(createRequest("닉네임", null, null)))
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

    private CreateMemberRequestDto createRequest(String nickname, String address, String profileImageUrl) {
        return CreateMemberRequestDto.builder()
                .memberUuid(MEMBER_UUID)
                .nickname(nickname)
                .address(address)
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
