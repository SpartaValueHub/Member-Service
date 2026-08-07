package com.sparta.member_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberAvailabilityResultDto {

    private final boolean available;
}
