package com.sparta.member_service.adaptor.out.mysql.entity;

import com.sparta.member_service.domain.enums.ConsentAction;
import com.sparta.member_service.domain.enums.ConsentChannel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/** 약관 동의 이력 — append-only, updated_at 없음 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "member_term_consents")
public class MemberTermConsentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_term_consent_id")
    private Long memberTermConsentId;

    @Column(name = "term_id", nullable = false)
    private Long termId;

    @Column(name = "member_uuid", nullable = false, length = 36)
    private String memberUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_action", nullable = false, length = 20)
    private ConsentAction consentAction;

    @Column(name = "action_at", nullable = false)
    private Instant actionAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_channel", nullable = false, length = 20)
    private ConsentChannel consentChannel;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    private MemberTermConsentEntity(
            Long termId,
            String memberUuid,
            ConsentAction consentAction,
            Instant actionAt,
            ConsentChannel consentChannel
    ) {
        this.termId = termId;
        this.memberUuid = memberUuid;
        this.consentAction = consentAction;
        this.actionAt = actionAt;
        this.consentChannel = consentChannel;
    }
}
