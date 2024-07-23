package com.project.trainingdiary.provider;

import com.project.trainingdiary.util.WorkoutImageUtil;
import com.project.trainingdiary.util.WorkoutVideoUtil;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class S3VideoProvider {

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${spring.cloud.aws.s3.temp-bucket}")
  private String tempBucket;

  private final S3Operations s3Operations;

  public String uploadVideo(MultipartFile video, String uuid)
      throws IOException, InterruptedException {
    String extension = WorkoutImageUtil.getExtension(WorkoutImageUtil.checkFileNameExist(video));
    String tempKey = "temp_" + uuid + "." + extension;
    String originalKey = "original_" + uuid + "." + extension;

    // 임시 버킷에 영상 업로드
    S3Resource tempS3Resource;
    try (InputStream inputStream = video.getInputStream()) {
      tempS3Resource = s3Operations.upload(tempBucket, tempKey, inputStream,
          ObjectMetadata.builder().contentType(video.getContentType()).build());
    }

    String tempVideoUrl = tempS3Resource.getURL().toExternalForm();
    String encodedVideoUrl = encodeAndUploadVideo(tempVideoUrl, originalKey,
        video.getContentType());

    // 임시 파일 삭제
    s3Operations.deleteObject(tempBucket, tempKey);

    return encodedVideoUrl;
  }

  public String uploadThumbnail(String encodedVideoUrl, String uuid)
      throws IOException, InterruptedException {
    String thumbnailKey = "thumb_" + uuid + ".png";
    S3Resource thumbS3Resource;
    try (InputStream inputStream = new URL(encodedVideoUrl).openStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      WorkoutVideoUtil.generateThumbnail(inputStream, outputStream);
      try (InputStream thumbnailInputStream = new ByteArrayInputStream(
          outputStream.toByteArray())) {
        thumbS3Resource = s3Operations.upload(bucket, thumbnailKey, thumbnailInputStream,
            ObjectMetadata.builder().contentType("image/png").build());
      }
    }
    return thumbS3Resource.getURL().toExternalForm();
  }

  private String encodeAndUploadVideo(String tempVideoUrl, String originalKey, String contentType)
      throws IOException, InterruptedException {
    S3Resource videoS3Resource;
    try (InputStream inputStream = new URL(tempVideoUrl).openStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      WorkoutVideoUtil.encodeVideo(inputStream, outputStream);
      try (InputStream encodedInputStream = new ByteArrayInputStream(outputStream.toByteArray())) {
        videoS3Resource = s3Operations.upload(bucket, originalKey, encodedInputStream,
            ObjectMetadata.builder().contentType(contentType).build());
      }
    }
    return videoS3Resource.getURL().toExternalForm();
  }

}
