package com.sparta.member_service.application.service;

import com.sparta.member_service.application.exception.ForbiddenException;
import com.sparta.member_service.application.exception.MediaInvalidRequestException;
import com.sparta.member_service.config.MediaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MediaObjectKeyPolicyTest {

	private static final String MEMBER_UUID = "550e8400-e29b-41d4-a716-446655440000";
	private static final String OTHER_UUID = "11111111-1111-1111-1111-111111111111";

	private MediaObjectKeyPolicy policy;

	@BeforeEach
	void setUp() {
		policy = new MediaObjectKeyPolicy(testMediaProperties());
	}

	@Test
	void createPendingKey_usesPendingProfilesPrefix() {
		String key = policy.createPendingKey(MEMBER_UUID, "image/jpeg");

		assertThat(key).startsWith("pending/profiles/" + MEMBER_UUID + "/");
		assertThat(key).endsWith(".jpg");
	}

	@Test
	void createPendingKey_fallsBackWhenExtensionMapEmpty() {
		MediaProperties emptyMap = new MediaProperties();
		emptyMap.setPendingPrefix("pending/");
		emptyMap.setConfirmedPrefix("profiles/");
		emptyMap.setExtensionByContentType(Map.of());
		MediaObjectKeyPolicy emptyPolicy = new MediaObjectKeyPolicy(emptyMap);

		String key = emptyPolicy.createPendingKey(MEMBER_UUID, "image/jpeg");

		assertThat(key).startsWith("pending/profiles/" + MEMBER_UUID + "/");
		assertThat(key).endsWith(".jpg");
	}

	@Test
	void requireContentType_stripsCharsetParameter() {
		assertThat(policy.requireContentType("image/jpeg; charset=UTF-8")).isEqualTo("image/jpeg");
	}

	@Test
	void resolve_promotesOwnPendingUrlToConfirmed() {
		String pendingUrl = "https://dxxxx.cloudfront.net/pending/profiles/" + MEMBER_UUID + "/a1.jpg";

		MediaObjectRef ref = policy.resolve(MEMBER_UUID, pendingUrl);

		assertThat(ref.isPending()).isTrue();
		assertThat(ref.getSourceKey()).isEqualTo("pending/profiles/" + MEMBER_UUID + "/a1.jpg");
		assertThat(ref.getDestinationKey()).isEqualTo("profiles/" + MEMBER_UUID + "/a1.jpg");
		assertThat(ref.getPublicUrl()).isEqualTo("https://dxxxx.cloudfront.net/profiles/" + MEMBER_UUID + "/a1.jpg");
	}

	@Test
	void resolve_keepsOwnConfirmedUrl() {
		String confirmedUrl = "https://dxxxx.cloudfront.net/profiles/" + MEMBER_UUID + "/a1.jpg";

		MediaObjectRef ref = policy.resolve(MEMBER_UUID, confirmedUrl);

		assertThat(ref.getState()).isEqualTo(MediaObjectRef.State.CONFIRMED);
		assertThat(ref.getPublicUrl()).isEqualTo(confirmedUrl);
	}

	@Test
	void resolve_allowsDefaultAvatarPassthrough() {
		MediaObjectRef ref = policy.resolve(MEMBER_UUID, "/images/default-profile.png");

		assertThat(ref.getState()).isEqualTo(MediaObjectRef.State.PASSTHROUGH);
		assertThat(ref.getPublicUrl()).isEqualTo("/images/default-profile.png");
	}

	@Test
	void resolve_rejectsOtherMemberPending() {
		String otherPending = "https://dxxxx.cloudfront.net/pending/profiles/" + OTHER_UUID + "/a1.jpg";

		assertThatThrownBy(() -> policy.resolve(MEMBER_UUID, otherPending))
				.isInstanceOf(ForbiddenException.class)
				.extracting(ex -> ((ForbiddenException) ex).getCode())
				.isEqualTo("FORBIDDEN");
	}

	@Test
	void resolve_rejectsForeignHost() {
		assertThatThrownBy(() -> policy.resolve(MEMBER_UUID, "https://evil.example/pending/profiles/" + MEMBER_UUID + "/a1.jpg"))
				.isInstanceOf(MediaInvalidRequestException.class)
				.extracting(ex -> ((MediaInvalidRequestException) ex).getCode())
				.isEqualTo("INVALID_MEDIA_KEY");
	}

	@Test
	void confirmedKeyIfOwned_returnsOnlyConfirmedOwnKey() {
		assertThat(policy.confirmedKeyIfOwned(MEMBER_UUID, "https://dxxxx.cloudfront.net/profiles/" + MEMBER_UUID + "/a1.jpg"))
				.contains("profiles/" + MEMBER_UUID + "/a1.jpg");
		assertThat(policy.confirmedKeyIfOwned(MEMBER_UUID, "https://dxxxx.cloudfront.net/pending/profiles/" + MEMBER_UUID + "/a1.jpg"))
				.isEmpty();
		assertThat(policy.confirmedKeyIfOwned(MEMBER_UUID, "/images/default-profile.png"))
				.isEmpty();
	}

	private MediaProperties testMediaProperties() {
		MediaProperties properties = new MediaProperties();
		properties.setS3Bucket("valuehub-media-test");
		properties.setCloudfrontBaseUrl("https://dxxxx.cloudfront.net");
		properties.setPendingPrefix("pending/");
		properties.setConfirmedPrefix("profiles/");
		properties.setExtensionByContentType(Map.of(
				"image/jpeg", "jpg",
				"image/png", "png",
				"image/webp", "webp",
				"image/gif", "gif"
		));
		properties.setPassthroughUrls(List.of("/images/default-profile.png"));
		return properties;
	}
}
