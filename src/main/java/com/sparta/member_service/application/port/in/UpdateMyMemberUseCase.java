package com.sparta.member_service.application.port.in;

import com.sparta.member_service.application.port.in.dto.GetMyMemberResultDto;
import com.sparta.member_service.application.port.in.dto.UpdateMyMemberCommand;

public interface UpdateMyMemberUseCase {

	GetMyMemberResultDto updateMyMember(UpdateMyMemberCommand command);
}
