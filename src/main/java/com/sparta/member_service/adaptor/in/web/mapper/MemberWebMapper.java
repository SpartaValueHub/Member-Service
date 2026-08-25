package com.sparta.member_service.adaptor.in.web.mapper;

import com.sparta.member_service.adaptor.in.web.vo.CreateMemberRequestVo;
import com.sparta.member_service.adaptor.in.web.vo.CreateMemberResponseVo;
import com.sparta.member_service.adaptor.in.web.vo.IssuePresignedUploadRequestVo;
import com.sparta.member_service.adaptor.in.web.vo.IssuePresignedUploadResponseVo;
import com.sparta.member_service.adaptor.in.web.vo.MemberAvailabilityResponseVo;
import com.sparta.member_service.adaptor.in.web.vo.MemberProfileResponseVo;
import com.sparta.member_service.adaptor.in.web.vo.MemberPublicProfileResponseVo;
import com.sparta.member_service.adaptor.in.web.vo.TermConsentItemVo;
import com.sparta.member_service.adaptor.in.web.vo.UpdateMyMemberRequestVo;
import com.sparta.member_service.application.port.in.dto.CreateMemberRequestDto;
import com.sparta.member_service.application.port.in.dto.CreateMemberResultDto;
import com.sparta.member_service.application.port.in.dto.GetMemberPublicProfileResultDto;
import com.sparta.member_service.application.port.in.dto.GetMyMemberResultDto;
import com.sparta.member_service.application.port.in.dto.IssuePresignedUploadCommand;
import com.sparta.member_service.application.port.in.dto.IssuePresignedUploadResultDto;
import com.sparta.member_service.application.port.in.dto.MemberAvailabilityResultDto;
import com.sparta.member_service.application.port.in.dto.TermConsentItemDto;
import com.sparta.member_service.application.port.in.dto.UpdateMyMemberCommand;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class MemberWebMapper {

    public CreateMemberRequestDto toDto(CreateMemberRequestVo vo) {
        return CreateMemberRequestDto.builder()
                .memberUuid(vo.getMemberUuid())
                .nickname(vo.getNickname())
                .profileImageUrl(vo.getProfileImageUrl())
                .address(vo.getAddress())
                .termConsents(toConsentDtos(vo.getTermConsents()))
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

    public MemberPublicProfileResponseVo toVo(GetMemberPublicProfileResultDto dto) {
        return MemberPublicProfileResponseVo.builder()
                .memberUuid(dto.getMemberUuid())
                .nickname(dto.getNickname())
                .profileImageUrl(dto.getProfileImageUrl())
                .build();
    }

    public IssuePresignedUploadCommand toPresignedCommand(String memberUuid, IssuePresignedUploadRequestVo vo) {
        return IssuePresignedUploadCommand.builder()
                .memberUuid(memberUuid)
                .contentType(vo.getContentType())
                .contentLength(vo.getContentLength())
                .build();
    }

    public IssuePresignedUploadResponseVo toVo(IssuePresignedUploadResultDto dto) {
        return IssuePresignedUploadResponseVo.builder()
                .uploadUrl(dto.getUploadUrl())
                .s3Key(dto.getS3Key())
                .publicUrl(dto.getPublicUrl())
                .expiresInSeconds(dto.getExpiresInSeconds())
                .build();
    }

    public UpdateMyMemberCommand toUpdateCommand(String memberUuid, UpdateMyMemberRequestVo vo) {
        return UpdateMyMemberCommand.builder()
                .memberUuid(memberUuid)
                .nickname(vo.getNickname())
                .profileImageUrl(vo.getProfileImageUrl())
                .address(vo.getAddress())
                .build();
    }

    private List<TermConsentItemDto> toConsentDtos(List<TermConsentItemVo> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(item -> TermConsentItemDto.builder()
                        .termCode(item.getTermCode())
                        .agreed(item.isAgreed())
                        .build())
                .toList();
    }
}
