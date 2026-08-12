package com.sparta.member_service.application.port.in;

import com.sparta.member_service.application.port.in.dto.GetMyMemberResultDto;

public interface GetMyMemberUseCase {

    GetMyMemberResultDto getMyMember(String memberUuid);
}
