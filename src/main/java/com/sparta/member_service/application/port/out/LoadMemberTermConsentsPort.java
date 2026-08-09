package com.sparta.member_service.application.port.out;

import com.sparta.member_service.domain.model.MemberTermConsentDomain;

import java.util.List;

public interface LoadMemberTermConsentsPort {

    List<MemberTermConsentDomain> findSignupConsentsByMemberUuid(String memberUuid);
}
