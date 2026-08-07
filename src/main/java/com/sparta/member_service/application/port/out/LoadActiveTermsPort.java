package com.sparta.member_service.application.port.out;

import com.sparta.member_service.domain.model.TermDomain;

import java.time.Instant;
import java.util.List;

public interface LoadActiveTermsPort {

    List<TermDomain> findAllActive();

    List<TermDomain> findAllCurrentlyEffective(Instant at);
}
