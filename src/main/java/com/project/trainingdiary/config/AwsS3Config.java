package com.project.trainingdiary.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsS3Config {

  @Value("${spring.cloud.aws.credentials.access-key}")
  private String accessKey;

  @Value("${spring.cloud.aws.credentials.secret-key}")
  private String secretKey;

  @Value("${spring.cloud.aws.region.static}")
  private String region;

  @Bean
  public AwsCredentialsProvider customAwsCredentialsProvider() {
    return () -> new AwsCredentials() {
      @Override
      public String accessKeyId() {
        return accessKey;
      }

      @Override
      public String secretAccessKey() {
        return secretKey;
      }
    };
  }

  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .credentialsProvider(customAwsCredentialsProvider())
        .region(customAwsRegionProvider().getRegion())
        .build();
  }

  @Bean
  public AwsRegionProvider customAwsRegionProvider() {
    return () -> Region.of(region);
  }

}
