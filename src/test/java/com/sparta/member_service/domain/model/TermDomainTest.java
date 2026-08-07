package com.sparta.member_service.domain.model;

import com.sparta.member_service.domain.enums.TermCode;
import com.sparta.member_service.domain.enums.TermType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TermDomainTest {

    private static final Instant EFFECTIVE_AT = Instant.parse("2024-01-01T00:00:00Z");
    private static final Instant EXPIRED_AT = Instant.parse("2025-01-01T00:00:00Z");

    @Test
    void create_success() {
        TermDomain term = TermDomain.create(
                TermCode.PRIVACY_POLICY,
                " 개인정보 처리방침 ",
                TermType.PRIVACY,
                true,
                " 1.0 ",
                " content ",
                EFFECTIVE_AT,
                EXPIRED_AT
        );

        assertThat(term.getTermCode()).isEqualTo(TermCode.PRIVACY_POLICY);
        assertThat(term.getTermName()).isEqualTo("개인정보 처리방침");
        assertThat(term.getVersion()).isEqualTo("1.0");
        assertThat(term.isActive()).isTrue();
        assertThat(term.isRequired()).isTrue();
        assertThat(term.getCreatedAt()).isNull();
    }

    @Test
    void create_rejectsExpiredAtBeforeEffectiveAt() {
        assertThatThrownBy(() -> TermDomain.create(
                TermCode.PRIVACY_POLICY,
                "약관",
                TermType.PRIVACY,
                true,
                "1.0",
                null,
                EXPIRED_AT,
                EFFECTIVE_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiredAt");
    }

    @Test
    void isConsentableAt_trueWhenActiveAndWithinWindow() {
        TermDomain term = TermDomain.create(
                TermCode.EMAIL_MARKETING,
                "이메일 마케팅",
                TermType.MARKETING,
                false,
                "1.0",
                null,
                EFFECTIVE_AT,
                EXPIRED_AT
        );

        assertThat(term.isConsentableAt(EFFECTIVE_AT)).isTrue();
        assertThat(term.isConsentableAt(EXPIRED_AT.minusSeconds(1))).isTrue();
    }

    @Test
    void isConsentableAt_falseWhenInactive() {
        TermDomain term = TermDomain.reconstitute(
                1L,
                TermCode.SMS_MARKETING,
                "SMS",
                TermType.MARKETING,
                false,
                false,
                "1.0",
                null,
                EFFECTIVE_AT,
                null,
                null,
                null
        );

        assertThat(term.isConsentableAt(EFFECTIVE_AT.plusSeconds(1))).isFalse();
    }

    @Test
    void isConsentableAt_falseWhenBeforeEffectiveAt() {
        TermDomain term = TermDomain.create(
                TermCode.PRIVACY_POLICY,
                "약관",
                TermType.PRIVACY,
                true,
                "1.0",
                null,
                EFFECTIVE_AT,
                null
        );

        assertThat(term.isConsentableAt(EFFECTIVE_AT.minusSeconds(1))).isFalse();
    }

    @Test
    void isConsentableAt_falseWhenExpired() {
        TermDomain term = TermDomain.create(
                TermCode.PRIVACY_POLICY,
                "약관",
                TermType.PRIVACY,
                true,
                "1.0",
                null,
                EFFECTIVE_AT,
                EXPIRED_AT
        );

        assertThat(term.isConsentableAt(EXPIRED_AT)).isFalse();
    }

    @Test
    void assertConsentableAt_throwsWhenNotConsentable() {
        TermDomain term = TermDomain.reconstitute(
                1L,
                TermCode.PRIVACY_POLICY,
                "약관",
                TermType.PRIVACY,
                true,
                false,
                "1.0",
                null,
                EFFECTIVE_AT,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> term.assertConsentableAt(EFFECTIVE_AT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("동의할 수 없는 약관");
    }
}
