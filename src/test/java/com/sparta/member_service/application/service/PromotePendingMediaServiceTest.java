package com.sparta.member_service.application.service;

import com.sparta.member_service.application.exception.MediaInvalidRequestException;
import com.sparta.member_service.application.port.out.ObjectStoragePort;
import com.sparta.member_service.config.MediaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotePendingMediaServiceTest {

	private static final String MEMBER_UUID = "550e8400-e29b-41d4-a716-446655440000";

	@Mock
	private ObjectStoragePort objectStoragePort;

	private PromotePendingMediaService service;

	@BeforeEach
	void setUp() {
		MediaProperties properties = new MediaProperties();
		properties.setCloudfrontBaseUrl("https://dxxxx.cloudfront.net");
		properties.setPendingPrefix("pending/");
		properties.setConfirmedPrefix("profiles/");
		properties.setExtensionByContentType(Map.of("image/jpeg", "jpg"));
		properties.setPassthroughUrls(List.of("/images/default-profile.png"));
		service = new PromotePendingMediaService(new MediaObjectKeyPolicy(properties), objectStoragePort);
	}

	@Test
	void persistSingle_copiesPendingThenDeletesSource() {
		String pendingUrl = "https://dxxxx.cloudfront.net/pending/profiles/" + MEMBER_UUID + "/a1.jpg";
		when(objectStoragePort.exists("pending/profiles/" + MEMBER_UUID + "/a1.jpg")).thenReturn(true);

		String result = service.persistSingle(MEMBER_UUID, pendingUrl);

		assertThat(result).isEqualTo("https://dxxxx.cloudfront.net/profiles/" + MEMBER_UUID + "/a1.jpg");
		verify(objectStoragePort).copyObject(
				"pending/profiles/" + MEMBER_UUID + "/a1.jpg",
				"profiles/" + MEMBER_UUID + "/a1.jpg"
		);
		verify(objectStoragePort).deleteObject("pending/profiles/" + MEMBER_UUID + "/a1.jpg");
	}

	@Test
	void persistSingle_skipsCopyWhenAlreadyConfirmed() {
		String confirmedUrl = "https://dxxxx.cloudfront.net/profiles/" + MEMBER_UUID + "/a1.jpg";

		String result = service.persistSingle(MEMBER_UUID, confirmedUrl);

		assertThat(result).isEqualTo(confirmedUrl);
		verify(objectStoragePort, never()).copyObject(anyString(), anyString());
	}

	@Test
	void persistSingle_isIdempotentWhenDestinationAlreadyExists() {
		String pendingUrl = "https://dxxxx.cloudfront.net/pending/profiles/" + MEMBER_UUID + "/a1.jpg";
		when(objectStoragePort.exists("pending/profiles/" + MEMBER_UUID + "/a1.jpg")).thenReturn(false);
		when(objectStoragePort.exists("profiles/" + MEMBER_UUID + "/a1.jpg")).thenReturn(true);

		String result = service.persistSingle(MEMBER_UUID, pendingUrl);

		assertThat(result).isEqualTo("https://dxxxx.cloudfront.net/profiles/" + MEMBER_UUID + "/a1.jpg");
		verify(objectStoragePort, never()).copyObject(anyString(), anyString());
	}

	@Test
	void persistSingle_rejectsMissingObject() {
		String pendingUrl = "https://dxxxx.cloudfront.net/pending/profiles/" + MEMBER_UUID + "/a1.jpg";
		when(objectStoragePort.exists("pending/profiles/" + MEMBER_UUID + "/a1.jpg")).thenReturn(false);
		when(objectStoragePort.exists("profiles/" + MEMBER_UUID + "/a1.jpg")).thenReturn(false);

		assertThatThrownBy(() -> service.persistSingle(MEMBER_UUID, pendingUrl))
				.isInstanceOf(MediaInvalidRequestException.class)
				.extracting(ex -> ((MediaInvalidRequestException) ex).getCode())
				.isEqualTo("MEDIA_OBJECT_NOT_FOUND");
	}

	@Test
	void persistAll_rollsBackCopiedDestinationsWhenLaterCopyFails() {
		String first = "https://dxxxx.cloudfront.net/pending/profiles/" + MEMBER_UUID + "/a1.jpg";
		String second = "https://dxxxx.cloudfront.net/pending/profiles/" + MEMBER_UUID + "/a2.jpg";
		when(objectStoragePort.exists("pending/profiles/" + MEMBER_UUID + "/a1.jpg")).thenReturn(true);
		when(objectStoragePort.exists("pending/profiles/" + MEMBER_UUID + "/a2.jpg")).thenReturn(false);
		when(objectStoragePort.exists("profiles/" + MEMBER_UUID + "/a2.jpg")).thenReturn(false);

		assertThatThrownBy(() -> service.persistAll(MEMBER_UUID, List.of(first, second)))
				.isInstanceOf(MediaInvalidRequestException.class);

		verify(objectStoragePort).deleteObject("profiles/" + MEMBER_UUID + "/a1.jpg");
	}
}
