package com.sparta.member_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// S3·CloudFront Presigned 업로드 설정 (컨테이너 env 주입)
@Getter
@Setter
@ConfigurationProperties(prefix = "app.media")
public class MediaProperties {

	// S3 버킷명
	private String s3Bucket = "";
	// CloudFront 공개 base URL (끝 슬래시 없이)
	private String cloudfrontBaseUrl = "";
	// AWS 리전
	private String awsRegion = "ap-northeast-2";
	// 업로드 최대 바이트 (5MB)
	private long maxBytes = 5_242_880L;
	// Presigned URL 유효 초
	private int presignTtlSeconds = 300;
	// 미확정 객체 prefix (끝 슬래시 포함 권장)
	private String pendingPrefix = "pending/";
	// 확정 객체 prefix (끝 슬래시 포함 권장)
	private String confirmedPrefix = "profiles/";
	// Content-Type → 확장자
	private Map<String, String> extensionByContentType = new LinkedHashMap<>();
	// 승격 없이 그대로 저장할 URL (기본 아바타 등)
	private List<String> passthroughUrls = new ArrayList<>();
}
