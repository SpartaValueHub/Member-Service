package com.sparta.member_service.adaptor.in.web;

import com.sparta.member_service.adaptor.in.web.mapper.MemberWebMapper;
import com.sparta.member_service.adaptor.in.web.vo.CreateMemberRequestVo;
import com.sparta.member_service.adaptor.in.web.vo.CreateMemberResponseVo;
import com.sparta.member_service.adaptor.in.web.vo.IssuePresignedUploadRequestVo;
import com.sparta.member_service.adaptor.in.web.vo.IssuePresignedUploadResponseVo;
import com.sparta.member_service.adaptor.in.web.vo.MemberAvailabilityResponseVo;
import com.sparta.member_service.adaptor.in.web.vo.MemberProfileResponseVo;
import com.sparta.member_service.adaptor.in.web.vo.MemberPublicProfileResponseVo;
import com.sparta.member_service.adaptor.in.web.vo.UpdateMyMemberRequestVo;
import com.sparta.member_service.application.exception.UnauthorizedException;
import com.sparta.member_service.application.port.in.CheckNicknameAvailabilityUseCase;
import com.sparta.member_service.application.port.in.CreateMemberUseCase;
import com.sparta.member_service.application.port.in.GetMemberPublicProfileUseCase;
import com.sparta.member_service.application.port.in.GetMyMemberUseCase;
import com.sparta.member_service.application.port.in.IssuePresignedUploadUseCase;
import com.sparta.member_service.application.port.in.UpdateMyMemberUseCase;
import com.sparta.member_service.application.port.in.dto.CreateMemberRequestDto;
import com.sparta.member_service.application.port.in.dto.CreateMemberResultDto;
import com.sparta.member_service.application.port.in.dto.GetMemberPublicProfileResultDto;
import com.sparta.member_service.application.port.in.dto.GetMyMemberResultDto;
import com.sparta.member_service.application.port.in.dto.IssuePresignedUploadResultDto;
import com.sparta.member_service.application.port.in.dto.MemberAvailabilityResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 Inbound Controller — Gateway JWT 검증 후 X-Member-Uuid 헤더와 body memberUuid 일치를 확인한다.
 */
@Tag(name = "Member", description = "회원 프로필 API")
@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class MemberController {

    private static final String MEMBER_UUID_HEADER = "X-Member-Uuid";

    private final CreateMemberUseCase createMemberUseCase;
    private final CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;
    private final GetMyMemberUseCase getMyMemberUseCase;
    private final UpdateMyMemberUseCase updateMyMemberUseCase;
    private final GetMemberPublicProfileUseCase getMemberPublicProfileUseCase;
    private final IssuePresignedUploadUseCase issuePresignedUploadUseCase;
    private final MemberWebMapper memberWebMapper;

    @Operation(summary = "닉네임 중복 확인", description = "회원가입 전 닉네임 사용 가능 여부를 확인합니다.")
    @GetMapping("/members/check/nickname")
    public MemberAvailabilityResponseVo checkNickname(@RequestParam String nickname) {
        MemberAvailabilityResultDto resultDto =
                checkNicknameAvailabilityUseCase.checkNicknameAvailability(nickname);
        return memberWebMapper.toVo(resultDto);
    }

    @Operation(summary = "회원 프로필 생성", description = "회원가입 직후 프로필(닉네임·주소)을 생성합니다.")
    @PostMapping("/members")
    public ResponseEntity<CreateMemberResponseVo> createMember(
            @RequestHeader(value = MEMBER_UUID_HEADER, required = false) String headerMemberUuid,
            @RequestBody CreateMemberRequestVo requestVo
    ) {
        assertMemberUuidMatches(headerMemberUuid, requestVo.getMemberUuid());

        CreateMemberRequestDto requestDto = memberWebMapper.toDto(requestVo);
        CreateMemberResultDto resultDto = createMemberUseCase.createMember(requestDto);
        HttpStatus status = resultDto.isCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(memberWebMapper.toVo(resultDto));
    }

    @Operation(summary = "내 회원 프로필 조회", description = "Gateway JWT 검증 후 X-Member-Uuid 헤더로 본인 프로필을 조회합니다.")
    @GetMapping("/members/me")
    public MemberProfileResponseVo getMyMember(
            @RequestHeader(value = MEMBER_UUID_HEADER, required = false) String headerMemberUuid
    ) {
        String memberUuid = requireMemberUuid(headerMemberUuid);
        GetMyMemberResultDto resultDto = getMyMemberUseCase.getMyMember(memberUuid);
        return memberWebMapper.toVo(resultDto);
    }

    @Operation(summary = "내 회원 프로필 수정", description = "닉네임·프로필 이미지·주소를 부분 수정합니다. null 필드는 유지합니다.")
    @PatchMapping("/members/me")
    public MemberProfileResponseVo updateMyMember(
            @RequestHeader(value = MEMBER_UUID_HEADER, required = false) String headerMemberUuid,
            @RequestBody UpdateMyMemberRequestVo requestVo
    ) {
        String memberUuid = requireMemberUuid(headerMemberUuid);
        GetMyMemberResultDto resultDto = updateMyMemberUseCase.updateMyMember(
                memberWebMapper.toUpdateCommand(memberUuid, requestVo)
        );
        return memberWebMapper.toVo(resultDto);
    }

    @Operation(
            summary = "프로필 이미지 Presigned URL 발급",
            description = "클라이언트가 S3에 직접 PUT할 Presigned URL과 CloudFront publicUrl을 발급합니다."
    )
    @PostMapping("/members/me/media/presigned-url")
    public IssuePresignedUploadResponseVo issuePresignedUpload(
            @RequestHeader(value = MEMBER_UUID_HEADER, required = false) String headerMemberUuid,
            @RequestBody IssuePresignedUploadRequestVo requestVo
    ) {
        String memberUuid = requireMemberUuid(headerMemberUuid);
        IssuePresignedUploadResultDto resultDto = issuePresignedUploadUseCase.issuePresignedUpload(
                memberWebMapper.toPresignedCommand(memberUuid, requestVo)
        );
        return memberWebMapper.toVo(resultDto);
    }

    @Operation(
            summary = "회원 공개 프로필 조회",
            description = "memberUuid로 닉네임·프로필 이미지만 조회합니다. Chat 등 서비스 간 호출용입니다."
    )
    @GetMapping("/members/{memberUuid}/profile")
    public MemberPublicProfileResponseVo getMemberPublicProfile(@PathVariable String memberUuid) {
        GetMemberPublicProfileResultDto resultDto =
                getMemberPublicProfileUseCase.getMemberPublicProfile(memberUuid);
        return memberWebMapper.toVo(resultDto);
    }

    private String requireMemberUuid(String headerMemberUuid) {
        if (headerMemberUuid == null || headerMemberUuid.isBlank()) {
            throw new UnauthorizedException("MEMBER_AUTH_MISSING", "인증 정보가 없습니다.");
        }
        return headerMemberUuid.trim();
    }

    private void assertMemberUuidMatches(String headerMemberUuid, String bodyMemberUuid) {
        String memberUuid = requireMemberUuid(headerMemberUuid);
        if (bodyMemberUuid == null || bodyMemberUuid.isBlank()) {
            throw new UnauthorizedException("MEMBER_UUID_REQUIRED", "회원 식별자가 필요합니다.");
        }
        if (!memberUuid.equals(bodyMemberUuid.trim())) {
            throw new UnauthorizedException("MEMBER_UUID_MISMATCH", "회원 식별자가 일치하지 않습니다.");
        }
    }
}
