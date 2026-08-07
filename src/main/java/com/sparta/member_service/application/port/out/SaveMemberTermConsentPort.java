package com.sparta.member_service.application.port.out;

import com.sparta.member_service.domain.model.MemberTermConsentDomain;

import java.util.List;

public interface SaveMemberTermConsentPort {

    void saveAll(List<MemberTermConsentDomain> consents);
}
