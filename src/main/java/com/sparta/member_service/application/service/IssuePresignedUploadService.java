package com.sparta.member_service.application.service;

import com.sparta.member_service.application.exception.MediaConfigurationException;
import com.sparta.member_service.application.exception.MediaInvalidRequestException;
import com.sparta.member_service.application.port.in.IssuePresignedUploadUseCase;
import com.sparta.member_service.application.port.in.dto.IssuePresignedUploadCommand;
import com.sparta.member_service.application.port.in.dto.IssuePresignedUploadResultDto;
import com.sparta.member_service.application.port.out.PresignObjectPutPort;
import com.sparta.member_service.config.MediaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssuePresignedUploadService implements IssuePresignedUploadUseCase {

	// S3 Presign Port
	private final PresignObjectPutPort presignObjectPutPort;
	// key 규칙
	private final MediaObjectKeyPolicy mediaObjectKeyPolicy;
	// 미디어 설정
	private final MediaProperties mediaProperties;

	@Override
	public IssuePresignedUploadResultDto issuePresignedUpload(IssuePresignedUploadCommand command) {
		String memberUuid = requireMemberUuid(command.getMemberUuid());
		String contentType = mediaObjectKeyPolicy.requireContentType(command.getContentType());
		long contentLength = requireContentLength(command.getContentLength());

		assertMediaConfigured();

		String s3Key = mediaObjectKeyPolicy.createPendingKey(memberUuid, contentType);
		int expiresInSeconds = mediaProperties.getPresignTtlSeconds();
		String uploadUrl = presignObjectPutPort.createPutUrl(
				s3Key,
				contentType,
				contentLength,
				expiresInSeconds
		);

		return IssuePresignedUploadResultDto.builder()
				.uploadUrl(uploadUrl)
				.s3Key(s3Key)
				.publicUrl(mediaObjectKeyPolicy.toPublicUrl(s3Key))
				.expiresInSeconds(expiresInSeconds)
				.build();
	}

	private String requireMemberUuid(String memberUuid) {
		if (memberUuid == null || memberUuid.isBlank()) {
			throw new IllegalArgumentException("memberUuid는 필수입니다.");
		}
		return memberUuid.trim();
	}

	private long requireContentLength(Long contentLength) {
		if (contentLength == null || contentLength <= 0 || contentLength > mediaProperties.getMaxBytes()) {
			throw new MediaInvalidRequestException(
					"INVALID_CONTENT_LENGTH",
					"파일 크기는 1바이트 이상 " + mediaProperties.getMaxBytes() + "바이트 이하여야 합니다."
			);
		}
		return contentLength;
	}

	private void assertMediaConfigured() {
		if (mediaProperties.getS3Bucket() == null || mediaProperties.getS3Bucket().isBlank()) {
			throw new MediaConfigurationException("MEDIA_CONFIG_MISSING", "S3_BUCKET이 설정되지 않았습니다.");
		}
		if (mediaProperties.getCloudfrontBaseUrl() == null || mediaProperties.getCloudfrontBaseUrl().isBlank()) {
			throw new MediaConfigurationException("MEDIA_CONFIG_MISSING", "CLOUDFRONT_BASE_URL이 설정되지 않았습니다.");
		}
	}
}
