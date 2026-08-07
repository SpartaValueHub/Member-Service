package com.sparta.member_service.adaptor.out.mysql.entity;

import com.sparta.member_service.domain.enums.TermCode;
import com.sparta.member_service.domain.enums.TermType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/** terms 테이블 — term_code UNIQUE */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(
        name = "terms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_terms_term_code", columnNames = "term_code")
        }
)
public class TermEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "term_id")
    private Long termId;

    @Enumerated(EnumType.STRING)
    @Column(name = "term_code", nullable = false, length = 30)
    private TermCode termCode;

    @Column(name = "term_name", nullable = false, length = 100)
    private String termName;

    @Enumerated(EnumType.STRING)
    @Column(name = "term_type", nullable = false, length = 20)
    private TermType termType;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Lob
    @Column(name = "content")
    private String content;

    @Column(name = "effective_at", nullable = false)
    private Instant effectiveAt;

    @Column(name = "expired_at")
    private Instant expiredAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder
    private TermEntity(
            TermCode termCode,
            String termName,
            TermType termType,
            boolean isRequired,
            boolean isActive,
            String version,
            String content,
            Instant effectiveAt,
            Instant expiredAt
    ) {
        this.termCode = termCode;
        this.termName = termName;
        this.termType = termType;
        this.isRequired = isRequired;
        this.isActive = isActive;
        this.version = version;
        this.content = content;
        this.effectiveAt = effectiveAt;
        this.expiredAt = expiredAt;
    }

    /** termCode는 생성 후 변경 없음 */
    public void updateTerm(
            String termName,
            TermType termType,
            boolean isRequired,
            boolean isActive,
            String version,
            String content,
            Instant effectiveAt,
            Instant expiredAt
    ) {
        this.termName = termName;
        this.termType = termType;
        this.isRequired = isRequired;
        this.isActive = isActive;
        this.version = version;
        this.content = content;
        this.effectiveAt = effectiveAt;
        this.expiredAt = expiredAt;
    }
}
