package com.sparta.member_service.domain.model;

import com.sparta.member_service.domain.enums.TermCode;
import com.sparta.member_service.domain.enums.TermType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** 약관 마스터 도메인 — 동의 가능 여부는 활성·유효기간으로 판단 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermDomain {

    private Long termId;
    private TermCode termCode;
    private String termName;
    private TermType termType;
    private boolean isRequired;
    private boolean isActive;
    private String version;
    private String content;
    private Instant effectiveAt;
    private Instant expiredAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static TermDomain create(
            TermCode termCode,
            String termName,
            TermType termType,
            boolean isRequired,
            String version,
            String content,
            Instant effectiveAt,
            Instant expiredAt
    ) {
        validateTermCode(termCode);
        String trimmedTermName = validateAndTrimTermName(termName);
        validateTermType(termType);
        String trimmedVersion = validateAndTrimVersion(version);
        validateEffectiveAt(effectiveAt);
        validateExpiredAt(effectiveAt, expiredAt);

        return TermDomain.builder()
                .termCode(termCode)
                .termName(trimmedTermName)
                .termType(termType)
                .isRequired(isRequired)
                .isActive(true)
                .version(trimmedVersion)
                .content(trimNullable(content))
                .effectiveAt(effectiveAt)
                .expiredAt(expiredAt)
                .build();
    }

    public TermDomain updateContent(
            String termName,
            TermType termType,
            boolean isRequired,
            boolean isActive,
            String version,
            String content,
            Instant effectiveAt,
            Instant expiredAt
    ) {
        String trimmedTermName = validateAndTrimTermName(termName);
        validateTermType(termType);
        String trimmedVersion = validateAndTrimVersion(version);
        validateEffectiveAt(effectiveAt);
        validateExpiredAt(effectiveAt, expiredAt);

        return TermDomain.builder()
                .termId(this.termId)
                .termCode(this.termCode)
                .termName(trimmedTermName)
                .termType(termType)
                .isRequired(isRequired)
                .isActive(isActive)
                .version(trimmedVersion)
                .content(trimNullable(content))
                .effectiveAt(effectiveAt)
                .expiredAt(expiredAt)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public boolean isConsentableAt(Instant at) {
        if (!isActive) {
            return false;
        }
        if (at == null || effectiveAt == null || at.isBefore(effectiveAt)) {
            return false;
        }
        return expiredAt == null || at.isBefore(expiredAt);
    }

    public void assertConsentableAt(Instant at) {
        if (!isConsentableAt(at)) {
            throw new IllegalStateException("동의할 수 없는 약관입니다.");
        }
    }

    public static TermDomain reconstitute(
            Long termId,
            TermCode termCode,
            String termName,
            TermType termType,
            boolean isRequired,
            boolean isActive,
            String version,
            String content,
            Instant effectiveAt,
            Instant expiredAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return TermDomain.builder()
                .termId(termId)
                .termCode(termCode)
                .termName(termName)
                .termType(termType)
                .isRequired(isRequired)
                .isActive(isActive)
                .version(version)
                .content(content)
                .effectiveAt(effectiveAt)
                .expiredAt(expiredAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    private static void validateTermCode(TermCode termCode) {
        if (termCode == null) {
            throw new IllegalArgumentException("termCode는 필수입니다.");
        }
    }

    private static String validateAndTrimTermName(String termName) {
        if (termName == null || termName.isBlank()) {
            throw new IllegalArgumentException("termName은 필수입니다.");
        }
        String trimmed = termName.trim();
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("termName은 100자 이하여야 합니다.");
        }
        return trimmed;
    }

    private static void validateTermType(TermType termType) {
        if (termType == null) {
            throw new IllegalArgumentException("termType은 필수입니다.");
        }
    }

    private static String validateAndTrimVersion(String version) {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("version은 필수입니다.");
        }
        String trimmed = version.trim();
        if (trimmed.length() > 20) {
            throw new IllegalArgumentException("version은 20자 이하여야 합니다.");
        }
        return trimmed;
    }

    private static void validateEffectiveAt(Instant effectiveAt) {
        if (effectiveAt == null) {
            throw new IllegalArgumentException("effectiveAt은 필수입니다.");
        }
    }

    private static void validateExpiredAt(Instant effectiveAt, Instant expiredAt) {
        if (expiredAt != null && !expiredAt.isAfter(effectiveAt)) {
            throw new IllegalArgumentException("expiredAt은 effectiveAt 이후여야 합니다.");
        }
    }

    private static String trimNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private TermDomain(
            Long termId,
            TermCode termCode,
            String termName,
            TermType termType,
            boolean isRequired,
            boolean isActive,
            String version,
            String content,
            Instant effectiveAt,
            Instant expiredAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.termId = termId;
        this.termCode = termCode;
        this.termName = termName;
        this.termType = termType;
        this.isRequired = isRequired;
        this.isActive = isActive;
        this.version = version;
        this.content = content;
        this.effectiveAt = effectiveAt;
        this.expiredAt = expiredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
