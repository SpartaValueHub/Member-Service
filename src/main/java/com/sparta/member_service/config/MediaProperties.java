package com.sparta.member_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
}
