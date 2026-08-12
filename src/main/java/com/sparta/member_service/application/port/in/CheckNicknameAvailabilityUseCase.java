package com.sparta.member_service.application.port.in;

import com.sparta.member_service.application.port.in.dto.MemberAvailabilityResultDto;

public interface CheckNicknameAvailabilityUseCase {

    MemberAvailabilityResultDto checkNicknameAvailability(String nickname);
}
