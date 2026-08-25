package com.sparta.member_service.application.service;

import com.sparta.member_service.application.exception.MediaConfigurationException;
import com.sparta.member_service.application.exception.MediaInvalidRequestException;
import com.sparta.member_service.application.port.in.dto.IssuePresignedUploadCommand;
import com.sparta.member_service.application.port.in.dto.IssuePresignedUploadResultDto;
import com.sparta.member_service.application.port.out.PresignObjectPutPort;
import com.sparta.member_service.config.MediaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssuePresignedUploadServiceTest {

	private static final String MEMBER_UUID = "550e8400-e29b-41d4-a716-446655440000";

	@Mock
	private PresignObjectPutPort presignObjectPutPort;

	private MediaProperties mediaProperties;
	private IssuePresignedUploadService service;

	@BeforeEach
	void setUp() {
		mediaProperties = new MediaProperties();
		mediaProperties.setS3Bucket("valuehub-media-test");
		mediaProperties.setCloudfrontBaseUrl("https://dxxxx.cloudfront.net");
		mediaProperties.setAwsRegion("ap-northeast-2");
		mediaProperties.setMaxBytes(5_242_880L);
		mediaProperties.setPresignTtlSeconds(300);
		service = new IssuePresignedUploadService(presignObjectPutPort, mediaProperties);
	}

	@Test
	void issuePresignedUpload_returnsUploadAndPublicUrl() {
		when(presignObjectPutPort.createPutUrl(anyString(), eq("image/jpeg"), eq(1024L), eq(300)))
				.thenReturn("https://s3.example/upload");

		IssuePresignedUploadResultDto result = service.issuePresignedUpload(
				IssuePresignedUploadCommand.builder()
						.memberUuid(MEMBER_UUID)
						.contentType("image/jpeg")
						.contentLength(1024L)
						.build()
		);

		assertThat(result.getUploadUrl()).isEqualTo("https://s3.example/upload");
		assertThat(result.getS3Key()).startsWith("profiles/" + MEMBER_UUID + "/");
		assertThat(result.getS3Key()).endsWith(".jpg");
		assertThat(result.getPublicUrl()).startsWith("https://dxxxx.cloudfront.net/profiles/" + MEMBER_UUID + "/");
		assertThat(result.getExpiresInSeconds()).isEqualTo(300);
		verify(presignObjectPutPort).createPutUrl(anyString(), eq("image/jpeg"), eq(1024L), eq(300));
	}

	@Test
	void issuePresignedUpload_rejectsInvalidContentType() {
		assertThatThrownBy(() -> service.issuePresignedUpload(
				IssuePresignedUploadCommand.builder()
						.memberUuid(MEMBER_UUID)
						.contentType("application/pdf")
						.contentLength(1024L)
						.build()
		))
				.isInstanceOf(MediaInvalidRequestException.class)
				.extracting(ex -> ((MediaInvalidRequestException) ex).getCode())
				.isEqualTo("INVALID_CONTENT_TYPE");
	}

	@Test
	void issuePresignedUpload_rejectsTooLargeContentLength() {
		assertThatThrownBy(() -> service.issuePresignedUpload(
				IssuePresignedUploadCommand.builder()
						.memberUuid(MEMBER_UUID)
						.contentType("image/png")
						.contentLength(5_242_881L)
						.build()
		))
				.isInstanceOf(MediaInvalidRequestException.class)
				.extracting(ex -> ((MediaInvalidRequestException) ex).getCode())
				.isEqualTo("INVALID_CONTENT_LENGTH");
	}

	@Test
	void issuePresignedUpload_rejectsMissingBucket() {
		mediaProperties.setS3Bucket("");

		assertThatThrownBy(() -> service.issuePresignedUpload(
				IssuePresignedUploadCommand.builder()
						.memberUuid(MEMBER_UUID)
						.contentType("image/webp")
						.contentLength(100L)
						.build()
		))
				.isInstanceOf(MediaConfigurationException.class)
				.extracting(ex -> ((MediaConfigurationException) ex).getCode())
				.isEqualTo("MEDIA_CONFIG_MISSING");
	}
}
