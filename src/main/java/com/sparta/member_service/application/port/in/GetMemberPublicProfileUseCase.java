package com.sparta.member_service.application.port.in;

import com.sparta.member_service.application.port.in.dto.GetMemberPublicProfileResultDto;

public interface GetMemberPublicProfileUseCase {

    GetMemberPublicProfileResultDto getMemberPublicProfile(String memberUuid);
}
