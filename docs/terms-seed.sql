-- member_db.terms 초기 데이터 (TermsDataInitializer 삭제 후 수동 시드용)
-- 실행 전: USE member_db; (또는 환경에 맞는 DB 선택)
-- 조건: is_active=1, effective_at <= NOW(), expired_at IS NULL → GET /api/v1/terms/active 에 노출

INSERT INTO terms (
    term_code,
    term_name,
    term_type,
    is_required,
    is_active,
    version,
    content,
    effective_at,
    expired_at,
    created_at,
    updated_at
) VALUES
(
    'TERMS_OF_SERVICE',
    '이용약관',
    'SERVICE',
    1,
    1,
    '1.0',
    '제1조 (목적)
본 약관은 Value Hub(이하 "서비스")의 이용과 관련하여 회사와 회원 간의 권리·의무 및 책임사항을 규정함을 목적으로 합니다.

제2조 (회원가입)
회원은 본인 명의로 가입해야 하며, 허위 정보를 제공해서는 안 됩니다.',
    '2026-01-01 00:00:00',
    NULL,
    UTC_TIMESTAMP(),
    UTC_TIMESTAMP()
),
(
    'PRIVACY_POLICY',
    '개인정보 수집 및 이용',
    'PRIVACY',
    1,
    1,
    '1.0',
    '1. 수집 항목
- 필수: 이름, 이메일, 로그인 ID, 비밀번호, 휴대전화번호, 닉네임
- 선택: 마케팅 수신 동의 시 연락처

2. 이용 목적
회원 식별, 서비스 제공, 고객 문의 응대

3. 보유 기간
회원 탈퇴 시까지 (관련 법령에 따른 보존 기간 제외)',
    '2026-01-01 00:00:00',
    NULL,
    UTC_TIMESTAMP(),
    UTC_TIMESTAMP()
),
(
    'EMAIL_MARKETING',
    '이메일 마케팅 수신',
    'MARKETING',
    0,
    1,
    '1.0',
    '이메일을 통해 이벤트·프로모션·신규 서비스 안내를 받을 수 있습니다.
동의하지 않아도 서비스 이용에 제한은 없으며, 언제든 수신 거부할 수 있습니다.',
    '2026-01-01 00:00:00',
    NULL,
    UTC_TIMESTAMP(),
    UTC_TIMESTAMP()
),
(
    'SMS_MARKETING',
    'SMS 마케팅 수신',
    'MARKETING',
    0,
    1,
    '1.0',
    'SMS(문자)를 통해 이벤트·프로모션·신규 서비스 안내를 받을 수 있습니다.
동의하지 않아도 서비스 이용에 제한은 없으며, 언제든 수신 거부할 수 있습니다.',
    '2026-01-01 00:00:00',
    NULL,
    UTC_TIMESTAMP(),
    UTC_TIMESTAMP()
);
