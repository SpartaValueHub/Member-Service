package com.sparta.member_service.adaptor.in.web.mapper;

import com.sparta.member_service.adaptor.in.web.vo.CreateMemberRequestVo;
import com.sparta.member_service.adaptor.in.web.vo.CreateMemberResponseVo;
import com.sparta.member_service.adaptor.in.web.vo.MemberAvailabilityResponseVo;
import com.sparta.member_service.adaptor.in.web.vo.MemberProfileResponseVo;
import com.sparta.member_service.application.port.in.dto.CreateMemberRequestDto;
import com.sparta.member_service.application.port.in.dto.CreateMemberResultDto;
import com.sparta.member_service.application.port.in.dto.GetMyMemberResultDto;
import com.sparta.member_service.application.port.in.dto.MemberAvailabilityResultDto;
import org.springframework.stereotype.Component;

@Component
public class MemberWebMapper {

    public CreateMemberRequestDto toDto(CreateMemberRequestVo vo) {
        return CreateMemberRequestDto.builder()
                .memberUuid(vo.getMemberUuid())
                .nickname(vo.getNickname())
                .profileImageUrl(vo.getProfileImageUrl())
                .address(vo.getAddress())
                .build();
    }

    public CreateMemberResponseVo toVo(CreateMemberResultDto dto) {
        return CreateMemberResponseVo.builder()
                .memberUuid(dto.getMemberUuid())
                .nickname(dto.getNickname())
                .profileImageUrl(dto.getProfileImageUrl())
                .memberGrade(dto.getMemberGrade())
                .address(dto.getAddress())
                .build();
    }

    public MemberAvailabilityResponseVo toVo(MemberAvailabilityResultDto dto) {
        return MemberAvailabilityResponseVo.builder()
                .available(dto.isAvailable())
                .build();
    }

    public MemberProfileResponseVo toVo(GetMyMemberResultDto dto) {
        return MemberProfileResponseVo.builder()
                .memberUuid(dto.getMemberUuid())
                .nickname(dto.getNickname())
                .profileImageUrl(dto.getProfileImageUrl())
                .memberGrade(dto.getMemberGrade())
                .address(dto.getAddress())
                .build();
    }
}
