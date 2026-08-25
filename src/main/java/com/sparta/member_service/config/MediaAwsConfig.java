package com.sparta.member_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

// S3 Presigner — EC2 Instance Role / 로컬 DefaultCredentialsProvider
@Configuration
@EnableConfigurationProperties(MediaProperties.class)
public class MediaAwsConfig {

	@Bean(destroyMethod = "close")
	public S3Presigner s3Presigner(MediaProperties mediaProperties) {
		return S3Presigner.builder()
				.region(Region.of(mediaProperties.getAwsRegion()))
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
	}
}
