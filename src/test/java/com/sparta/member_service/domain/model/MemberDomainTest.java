package com.sparta.member_service.domain.model;

import com.sparta.member_service.domain.enums.MemberGrade;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberDomainTest {

    private static final String MEMBER_UUID = "550e8400-e29b-41d4-a716-446655440000";

    @Test
    void create_success() {
        MemberDomain member = MemberDomain.create(
                MEMBER_UUID,
                "  닉네임  ",
                " https://example.com/profile.png ",
                " 서울시 강남구 "
        );

        assertThat(member.getMemberUuid()).isEqualTo(MEMBER_UUID);
        assertThat(member.getNickname()).isEqualTo("닉네임");
        assertThat(member.getProfileImageUrl()).isEqualTo("https://example.com/profile.png");
        assertThat(member.getAddress()).isEqualTo("서울시 강남구");
        assertThat(member.getMemberGrade()).isEqualTo(MemberGrade.BRONZE);
        assertThat(member.isPremium()).isFalse();
        assertThat(member.isRegions()).isFalse();
        assertThat(member.getCreatedAt()).isNull();
        assertThat(member.getUpdatedAt()).isNull();
    }

    @Test
    void create_rejectsBlankMemberUuid() {
        assertThatThrownBy(() -> MemberDomain.create("  ", "nickname", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("memberUuid");
    }

    @Test
    void create_rejectsBlankNickname() {
        assertThatThrownBy(() -> MemberDomain.create(MEMBER_UUID, "  ", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nickname");
    }

    @Test
    void create_rejectsTooLongNickname() {
        assertThatThrownBy(() -> MemberDomain.create(MEMBER_UUID, "a".repeat(51), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nickname");
    }

    @Test
    void create_rejectsTooLongProfileImageUrl() {
        assertThatThrownBy(() -> MemberDomain.create(MEMBER_UUID, "nickname", "a".repeat(501), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("profileImageUrl");
    }

    @Test
    void create_rejectsTooLongAddress() {
        assertThatThrownBy(() -> MemberDomain.create(MEMBER_UUID, "nickname", null, "a".repeat(101)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("address");
    }

    @Test
    void normalizeNicknameForLookup_matchesCreateStoredValue() {
        MemberDomain member = MemberDomain.create(MEMBER_UUID, "  nickname  ", null, null);

        assertThat(MemberDomain.normalizeNicknameForLookup("  nickname  "))
                .isEqualTo(member.getNickname());
    }

    @Test
    void updateProfile_updatesMutableFields() {
        MemberDomain member = MemberDomain.create(MEMBER_UUID, "nickname", null, null);
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2024-06-01T00:00:00Z");
        MemberDomain persisted = MemberDomain.reconstitute(
                1L,
                member.getMemberUuid(),
                member.getNickname(),
                member.getProfileImageUrl(),
                member.getMemberGrade(),
                member.getAddress(),
                member.isPremium(),
                member.isRegions(),
                createdAt,
                updatedAt
        );

        MemberDomain updated = persisted.updateProfile(
                "newNick",
                "https://example.com/new.png",
                "부산",
                MemberGrade.GOLD,
                true
        );

        assertThat(updated.getMemberId()).isEqualTo(1L);
        assertThat(updated.getMemberUuid()).isEqualTo(MEMBER_UUID);
        assertThat(updated.getNickname()).isEqualTo("newNick");
        assertThat(updated.getMemberGrade()).isEqualTo(MemberGrade.GOLD);
        assertThat(updated.isPremium()).isTrue();
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
