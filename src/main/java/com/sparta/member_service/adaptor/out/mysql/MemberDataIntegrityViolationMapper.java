package com.sparta.member_service.adaptor.out.mysql;

import com.sparta.member_service.application.exception.DuplicateResourceException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Locale;
import java.util.Optional;

final class MemberDataIntegrityViolationMapper {

    private MemberDataIntegrityViolationMapper() {
    }

    static Optional<DuplicateResourceException> mapDuplicate(DataIntegrityViolationException ex) {
        String message = extractMessage(ex).toLowerCase(Locale.ROOT);
        if (message.contains("uk_member_member_uuid") || message.contains("member_uuid")) {
            return Optional.of(new DuplicateResourceException("MEMBER_DUPLICATE_UUID", "이미 등록된 회원입니다."));
        }
        if (message.contains("uk_member_nickname") || message.contains("nickname")) {
            return Optional.of(new DuplicateResourceException("MEMBER_DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다."));
        }
        return Optional.empty();
    }

    private static String extractMessage(DataIntegrityViolationException ex) {
        if (ex.getMostSpecificCause() != null && ex.getMostSpecificCause().getMessage() != null) {
            return ex.getMostSpecificCause().getMessage();
        }
        return ex.getMessage() == null ? "" : ex.getMessage();
    }
}
