# Member API

공통 Error Response:

```json
{
  "timestamp": "2026-08-04T08:00:00Z",
  "status": 400,
  "code": "ERROR_CODE",
  "message": "설명",
  "path": "/api/v1/..."
}
```

---

## 현재 유효 약관 목록

### Summary
회원가입 화면 등에 노출할 현재 유효한 약관 목록을 조회합니다. Gateway public path — JWT 불필요.

### Method · Path
`GET /api/v1/terms/active`

### Auth
불필요 (회원가입 전 public API)

### Request
없음

### Response (200)

배열. 각 항목:

| 필드 | 타입 |
|------|------|
| termId | number |
| termCode | string (`TERMS_OF_SERVICE` · `PRIVACY_POLICY` · `EMAIL_MARKETING` · `SMS_MARKETING`) |
| termName | string |
| termType | string (`SERVICE` · `PRIVACY` · `MARKETING`) |
| required | boolean |
| version | string |
| content | string |
| effectiveAt | string (ISO-8601) |

```json
[
  {
    "termId": 1,
    "termCode": "TERMS_OF_SERVICE",
    "termName": "이용약관",
    "termType": "SERVICE",
    "required": true,
    "version": "1.0",
    "content": "약관 본문...",
    "effectiveAt": "2026-01-01T00:00:00Z"
  }
]
```

조회 조건: `is_active=true`, `effective_at <= CURRENT_TIMESTAMP`, `expired_at IS NULL OR expired_at > CURRENT_TIMESTAMP`, `term_id ASC` 정렬.

### Errors

| status | code | 의미 |
|--------|------|------|
| 500 | INTERNAL_ERROR | 서버 오류 |

---

## 닉네임 중복 확인

### Summary
회원가입 전 닉네임 사용 가능 여부를 확인합니다. Gateway public path — JWT 불필요.

### Method · Path
`GET /api/v1/members/check/nickname?nickname={value}`

### Auth
불필요 (회원가입 전 public API)

### Request (Query)

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| nickname | string | O | trim 후 조회 |

### Response (200)

| 필드 | 타입 |
|------|------|
| available | boolean |

```json
{
  "available": true
}
```

### Errors

| status | code | 의미 |
|--------|------|------|
| 400 | INVALID_REQUEST | nickname blank 등 형식 오류 |

---

## 회원 프로필 생성

### Summary
회원가입 직후 auth-service `authUuid`와 동일한 `memberUuid`로 프로필(닉네임·주소)을 생성합니다. Gateway JWT 검증 후 `X-Member-Uuid` 헤더와 body `memberUuid`가 일치해야 합니다.

### Method · Path
`POST /api/v1/members`

### Auth
필요 — Gateway JWT (HttpOnly Cookie `vh_access_token` 또는 Bearer). Gateway가 `X-Member-Uuid` 헤더를 주입합니다.

### Request (Body)

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| memberUuid | string | O | auth-service sign-up 응답 authUuid와 동일 |
| nickname | string | O | 50자 이하, trim |
| profileImageUrl | string | X | 500자 이하 |
| address | string | X | 100자 이하 |
| termConsents | array | O | 약관 동의 목록. 활성·동의가능 필수 약관은 `agreed=true` 필수 |
| termConsents[].termCode | string | O | `TERMS_OF_SERVICE` · `PRIVACY_POLICY` · `EMAIL_MARKETING` · `SMS_MARKETING` |
| termConsents[].agreed | boolean | O | 동의 여부. 선택 약관은 `true`일 때만 이력 저장 |

```json
{
  "memberUuid": "550e8400-e29b-41d4-a716-446655440000",
  "nickname": "홍길동",
  "address": "서울특별시 강남구 테헤란로 123",
  "termConsents": [
    { "termCode": "TERMS_OF_SERVICE", "agreed": true },
    { "termCode": "PRIVACY_POLICY", "agreed": true },
    { "termCode": "EMAIL_MARKETING", "agreed": false },
    { "termCode": "SMS_MARKETING", "agreed": true }
  ]
}
```

### Response (201)

| 필드 | 타입 |
|------|------|
| memberUuid | string |
| nickname | string |
| profileImageUrl | string |
| memberGrade | string (BRONZE 등) |
| address | string |

```json
{
  "memberUuid": "550e8400-e29b-41d4-a716-446655440000",
  "nickname": "홍길동",
  "profileImageUrl": null,
  "memberGrade": "BRONZE",
  "address": "서울특별시 강남구 테헤란로 123"
}
```

### Errors

| status | code | 의미 |
|--------|------|------|
| 400 | INVALID_REQUEST | nickname·address·termConsents 등 형식 오류 |
| 400 | TERM_REQUIRED_CONSENT_MISSING | 필수 약관 미동의 |
| 400 | TERM_MASTER_MISSING | 동의 가능한 필수 약관 마스터 없음 |
| 401 | MEMBER_AUTH_MISSING | JWT·X-Member-Uuid 헤더 없음 |
| 401 | MEMBER_UUID_REQUIRED | body memberUuid 누락 |
| 401 | MEMBER_UUID_MISMATCH | X-Member-Uuid와 body memberUuid 불일치 |
| 409 | MEMBER_DUPLICATE_UUID | 이미 등록된 회원 |
| 409 | MEMBER_DUPLICATE_NICKNAME | 닉네임 중복 |
| 409 | MEMBER_PROFILE_CONFLICT | 동일 UUID에 다른 프로필로 재요청 |

---

## 내 회원 프로필 조회

### Summary
로그인한 회원의 프로필을 조회합니다. Gateway JWT 검증 후 `X-Member-Uuid` 헤더만 사용합니다.

### Method · Path
`GET /api/v1/members/me`

### Auth
필요 — Gateway JWT (HttpOnly Cookie `vh_access_token` 또는 Bearer). Gateway가 `X-Member-Uuid` 헤더를 주입합니다.

### Request
없음 (Path/Query/Body 없음). 회원 식별은 `X-Member-Uuid` 헤더만 사용합니다.

### Response (200)

| 필드 | 타입 |
|------|------|
| memberUuid | string |
| nickname | string |
| profileImageUrl | string |
| memberGrade | string (BRONZE 등) |
| address | string |

```json
{
  "memberUuid": "550e8400-e29b-41d4-a716-446655440000",
  "nickname": "홍길동",
  "profileImageUrl": null,
  "memberGrade": "BRONZE",
  "address": "서울특별시 강남구 테헤란로 123"
}
```

### Errors

| status | code | 의미 |
|--------|------|------|
| 401 | MEMBER_AUTH_MISSING | JWT·X-Member-Uuid 헤더 없음 |
| 404 | MEMBER_NOT_FOUND | 프로필 미등록 |

---

## 회원 공개 프로필 조회

### Summary
`memberUuid`로 닉네임·프로필 이미지만 조회합니다. Chat-Service Feign 등 **서비스 간 호출**용입니다.

### Method · Path
`GET /api/v1/members/{memberUuid}/profile`

### Auth
유저 JWT·`X-Member-Uuid` 불필요. 서비스 간 내부 호출을 전제로 합니다. (Gateway public 노출 여부는 별도)

### Request (Path)

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| memberUuid | string | O | trim 후 조회. blank 불가 |

### Response (200)

| 필드 | 타입 |
|------|------|
| memberUuid | string |
| nickname | string |
| profileImageUrl | string |

```json
{
  "memberUuid": "550e8400-e29b-41d4-a716-446655440000",
  "nickname": "홍길동",
  "profileImageUrl": null
}
```

### Errors

| status | code | 의미 |
|--------|------|------|
| 400 | INVALID_REQUEST | memberUuid blank 등 형식 오류 |
| 404 | MEMBER_NOT_FOUND | 프로필 미등록 |
