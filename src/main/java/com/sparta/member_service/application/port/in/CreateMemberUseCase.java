package com.sparta.member_service.application.port.in;

import com.sparta.member_service.application.port.in.dto.CreateMemberRequestDto;
import com.sparta.member_service.application.port.in.dto.CreateMemberResultDto;

public interface CreateMemberUseCase {

    CreateMemberResultDto createMember(CreateMemberRequestDto requestDto);
}
