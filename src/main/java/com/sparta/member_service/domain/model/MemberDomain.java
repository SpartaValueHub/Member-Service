package com.sparta.member_service.domain.model;

import com.sparta.member_service.domain.enums.MemberGrade;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 회원 프로필 도메인 — memberUuid가 외부 식별자(PK 아님).
 * auth-service authUuid와 동일 값으로 연결되는 것을 전제로 memberUuid를 입력받는다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDomain {

    private Long memberId;
    private String memberUuid;
    private String nickname;
    private String profileImageUrl;
    private MemberGrade memberGrade;
    private String address;
    private boolean isPremium;
    private boolean isRegions;
    private Instant createdAt;
    private Instant updatedAt;

    public static MemberDomain create(
            String memberUuid,
            String nickname,
            String profileImageUrl,
            String address
    ) {
        validateMemberUuid(memberUuid);
        String trimmedNickname = validateAndTrimNickname(nickname);
        String trimmedProfileImageUrl = trimNullable(profileImageUrl);
        validateProfileImageUrl(trimmedProfileImageUrl);
        String trimmedAddress = trimNullable(address);
        validateAddress(trimmedAddress);

        return MemberDomain.builder()
                .memberUuid(memberUuid.trim())
                .nickname(trimmedNickname)
                .profileImageUrl(trimmedProfileImageUrl)
                .memberGrade(MemberGrade.BRONZE)
                .address(trimmedAddress)
                .isPremium(false)
                .isRegions(false)
                .build();
    }

    public MemberDomain updateProfile(
            String nickname,
            String profileImageUrl,
            String address,
            MemberGrade memberGrade,
            boolean isPremium
    ) {
        String trimmedNickname = validateAndTrimNickname(nickname);
        String trimmedProfileImageUrl = trimNullable(profileImageUrl);
        validateProfileImageUrl(trimmedProfileImageUrl);
        String trimmedAddress = trimNullable(address);
        validateAddress(trimmedAddress);
        if (memberGrade == null) {
            throw new IllegalArgumentException("memberGrade는 필수입니다.");
        }

        return MemberDomain.builder()
                .memberId(this.memberId)
                .memberUuid(this.memberUuid)
                .nickname(trimmedNickname)
                .profileImageUrl(trimmedProfileImageUrl)
                .memberGrade(memberGrade)
                .address(trimmedAddress)
                .isPremium(isPremium)
                .isRegions(this.isRegions)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public static MemberDomain reconstitute(
            Long memberId,
            String memberUuid,
            String nickname,
            String profileImageUrl,
            MemberGrade memberGrade,
            String address,
            boolean isPremium,
            boolean isRegions,
            Instant createdAt,
            Instant updatedAt
    ) {
        return MemberDomain.builder()
                .memberId(memberId)
                .memberUuid(memberUuid)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .memberGrade(memberGrade)
                .address(address)
                .isPremium(isPremium)
                .isRegions(isRegions)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /** 닉네임 중복 조회용 — create 저장값과 동일한 trim */
    public static String normalizeNicknameForLookup(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("nickname은 필수입니다.");
        }
        return nickname.trim();
    }

    private static void validateMemberUuid(String memberUuid) {
        if (memberUuid == null || memberUuid.isBlank()) {
            throw new IllegalArgumentException("memberUuid는 필수입니다.");
        }
    }

    private static String validateAndTrimNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("nickname은 필수입니다.");
        }
        String trimmed = nickname.trim();
        if (trimmed.length() > 50) {
            throw new IllegalArgumentException("nickname은 50자 이하여야 합니다.");
        }
        return trimmed;
    }

    private static void validateProfileImageUrl(String profileImageUrl) {
        if (profileImageUrl != null && profileImageUrl.length() > 500) {
            throw new IllegalArgumentException("profileImageUrl은 500자 이하여야 합니다.");
        }
    }

    private static void validateAddress(String address) {
        if (address != null && address.length() > 100) {
            throw new IllegalArgumentException("address는 100자 이하여야 합니다.");
        }
    }

    private static String trimNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private MemberDomain(
            Long memberId,
            String memberUuid,
            String nickname,
            String profileImageUrl,
            MemberGrade memberGrade,
            String address,
            boolean isPremium,
            boolean isRegions,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.memberId = memberId;
        this.memberUuid = memberUuid;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.memberGrade = memberGrade;
        this.address = address;
        this.isPremium = isPremium;
        this.isRegions = isRegions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
