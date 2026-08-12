package com.sparta.member_service.domain.model;

import com.sparta.member_service.domain.enums.ConsentAction;
import com.sparta.member_service.domain.enums.ConsentChannel;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** 약관 동의 이력 — append-only, member_uuid 단독 UNIQUE 없음 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTermConsentDomain {

    private Long memberTermConsentId;
    private Long termId;
    private String memberUuid;
    private ConsentAction consentAction;
    private Instant actionAt;
    private ConsentChannel consentChannel;
    private Instant createdAt;

    public static MemberTermConsentDomain record(
            TermDomain term,
            String memberUuid,
            ConsentAction consentAction,
            ConsentChannel consentChannel,
            Instant actionAt
    ) {
        if (term == null) {
            throw new IllegalArgumentException("term은 필수입니다.");
        }
        if (term.getTermId() == null) {
            throw new IllegalArgumentException("termId는 필수입니다.");
        }
        term.assertConsentableAt(actionAt);

        return record(
                term.getTermId(),
                memberUuid,
                consentAction,
                consentChannel,
                actionAt
        );
    }

    public static MemberTermConsentDomain record(
            Long termId,
            String memberUuid,
            ConsentAction consentAction,
            ConsentChannel consentChannel,
            Instant actionAt
    ) {
        validateTermId(termId);
        validateMemberUuid(memberUuid);
        validateConsentAction(consentAction);
        validateConsentChannel(consentChannel);
        validateActionAt(actionAt);

        return MemberTermConsentDomain.builder()
                .termId(termId)
                .memberUuid(memberUuid.trim())
                .consentAction(consentAction)
                .actionAt(actionAt)
                .consentChannel(consentChannel)
                .build();
    }

    public static MemberTermConsentDomain reconstitute(
            Long memberTermConsentId,
            Long termId,
            String memberUuid,
            ConsentAction consentAction,
            Instant actionAt,
            ConsentChannel consentChannel,
            Instant createdAt
    ) {
        return MemberTermConsentDomain.builder()
                .memberTermConsentId(memberTermConsentId)
                .termId(termId)
                .memberUuid(memberUuid)
                .consentAction(consentAction)
                .actionAt(actionAt)
                .consentChannel(consentChannel)
                .createdAt(createdAt)
                .build();
    }

    private static void validateTermId(Long termId) {
        if (termId == null) {
            throw new IllegalArgumentException("termId는 필수입니다.");
        }
    }

    private static void validateMemberUuid(String memberUuid) {
        if (memberUuid == null || memberUuid.isBlank()) {
            throw new IllegalArgumentException("memberUuid는 필수입니다.");
        }
    }

    private static void validateConsentAction(ConsentAction consentAction) {
        if (consentAction == null) {
            throw new IllegalArgumentException("consentAction은 필수입니다.");
        }
    }

    private static void validateConsentChannel(ConsentChannel consentChannel) {
        if (consentChannel == null) {
            throw new IllegalArgumentException("consentChannel은 필수입니다.");
        }
    }

    private static void validateActionAt(Instant actionAt) {
        if (actionAt == null) {
            throw new IllegalArgumentException("actionAt은 필수입니다.");
        }
    }

    @Builder(access = AccessLevel.PRIVATE)
    private MemberTermConsentDomain(
            Long memberTermConsentId,
            Long termId,
            String memberUuid,
            ConsentAction consentAction,
            Instant actionAt,
            ConsentChannel consentChannel,
            Instant createdAt
    ) {
        this.memberTermConsentId = memberTermConsentId;
        this.termId = termId;
        this.memberUuid = memberUuid;
        this.consentAction = consentAction;
        this.actionAt = actionAt;
        this.consentChannel = consentChannel;
        this.createdAt = createdAt;
    }
}
