package com.sparta.member_service.application.port.in;

import com.sparta.member_service.application.port.in.dto.ActiveTermResultDto;

import java.util.List;

public interface ListActiveTermsUseCase {

    List<ActiveTermResultDto> listActiveTerms();
}
