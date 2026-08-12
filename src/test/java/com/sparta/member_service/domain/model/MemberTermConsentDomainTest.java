package com.sparta.member_service.domain.model;

import com.sparta.member_service.domain.enums.ConsentAction;
import com.sparta.member_service.domain.enums.ConsentChannel;
import com.sparta.member_service.domain.enums.TermCode;
import com.sparta.member_service.domain.enums.TermType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTermConsentDomainTest {

    private static final String MEMBER_UUID = "550e8400-e29b-41d4-a716-446655440000";
    private static final Instant ACTION_AT = Instant.parse("2024-06-01T12:00:00Z");
    private static final Instant EFFECTIVE_AT = Instant.parse("2024-01-01T00:00:00Z");

    @Test
    void record_successWithConsentableTerm() {
        TermDomain term = TermDomain.reconstitute(
                10L,
                TermCode.PRIVACY_POLICY,
                "개인정보",
                TermType.PRIVACY,
                true,
                true,
                "1.0",
                null,
                EFFECTIVE_AT,
                null,
                null,
                null
        );

        MemberTermConsentDomain consent = MemberTermConsentDomain.record(
                term,
                MEMBER_UUID,
                ConsentAction.AGREE,
                ConsentChannel.SIGN_UP,
                ACTION_AT
        );

        assertThat(consent.getTermId()).isEqualTo(10L);
        assertThat(consent.getMemberUuid()).isEqualTo(MEMBER_UUID);
        assertThat(consent.getConsentAction()).isEqualTo(ConsentAction.AGREE);
        assertThat(consent.getConsentChannel()).isEqualTo(ConsentChannel.SIGN_UP);
        assertThat(consent.getActionAt()).isEqualTo(ACTION_AT);
        assertThat(consent.getCreatedAt()).isNull();
    }

    @Test
    void record_rejectsWhenTermNotConsentable() {
        TermDomain inactiveTerm = TermDomain.reconstitute(
                10L,
                TermCode.EMAIL_MARKETING,
                "이메일",
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

        assertThatThrownBy(() -> MemberTermConsentDomain.record(
                inactiveTerm,
                MEMBER_UUID,
                ConsentAction.AGREE,
                ConsentChannel.SIGN_UP,
                ACTION_AT
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("동의할 수 없는 약관");
    }

    @Test
    void record_rejectsBlankMemberUuid() {
        assertThatThrownBy(() -> MemberTermConsentDomain.record(
                1L,
                "  ",
                ConsentAction.WITHDRAW,
                ConsentChannel.MY_PAGE,
                ACTION_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("memberUuid");
    }

    @Test
    void record_rejectsNullConsentAction() {
        assertThatThrownBy(() -> MemberTermConsentDomain.record(
                1L,
                MEMBER_UUID,
                null,
                ConsentChannel.SIGN_UP,
                ACTION_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("consentAction");
    }
}
