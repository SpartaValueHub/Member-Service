package com.sparta.member_service.domain.enums;

/** 약관 동의 채널 — member_term_consents.consent_channel */
public enum ConsentChannel {
    /** 회원가입 중 동의 */
    SIGN_UP,
    /** 마이페이지에서 동의 또는 철회 */
    MY_PAGE,
    /** 관리자가 처리 */
    ADMIN,
    /** 외부 API 또는 내부 시스템 처리 */
    API
}
