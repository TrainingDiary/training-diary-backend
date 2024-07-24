package com.project.trainingdiary.provider;

import com.project.trainingdiary.util.MediaUtil;
import com.project.trainingdiary.util.VideoUtil;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Resource;
import java.io.File;
import java.io.FileInputStream;
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
    String extension = MediaUtil.getExtension(MediaUtil.checkFileNameExist(video));
    String tempKey = "temp_" + uuid + "." + extension;
    String originalKey =
        "original_" + uuid + "." + (extension.equalsIgnoreCase("mov") ? "mp4" : extension);

    String contentType = video.getContentType();
    if ("mov".equalsIgnoreCase(extension)) {
      contentType = "video/mp4";
    }

    // 임시 버킷에 영상 업로드
    S3Resource tempS3Resource;
    try (InputStream inputStream = video.getInputStream()) {
      tempS3Resource = s3Operations.upload(tempBucket, tempKey, inputStream,
          ObjectMetadata.builder().contentType(video.getContentType()).build());
    }

    String tempVideoUrl = tempS3Resource.getURL().toExternalForm();
    String encodedVideoUrl = encodeAndUploadVideo(
        tempVideoUrl, originalKey, contentType, extension);

    // 임시 파일 삭제
    s3Operations.deleteObject(tempBucket, tempKey);

    return encodedVideoUrl;
  }

  public String uploadThumbnail(String encodedVideoUrl, String uuid)
      throws IOException, InterruptedException {
    String thumbnailKey = "thumb_" + uuid + ".png";

    String tmpPath = "/tmp/thumb_" + uuid + ".png";
    String thumbPath = VideoUtil.generateThumbnail(encodedVideoUrl, tmpPath);

    S3Resource thumbS3Resource;

    try (InputStream inputStream = new FileInputStream(thumbPath)) {
      thumbS3Resource = s3Operations.upload(bucket, thumbnailKey, inputStream,
          ObjectMetadata.builder().contentType("image/png").build());
    }

    // 임시 파일 삭제
    new File(tmpPath).delete();

    return thumbS3Resource.getURL().toExternalForm();
  }

  private String encodeAndUploadVideo(
      String tempVideoUrl,
      String originalKey,
      String contentType,
      String extension
  ) throws IOException, InterruptedException {
    // 임시 파일 경로 설정
    String tmpPath = "/tmp/original_" + originalKey;

    S3Resource videoS3Resource;
    if ("mov".equalsIgnoreCase(extension)) {
      String encodedVideoPath = VideoUtil.encodeVideo(tempVideoUrl, tmpPath);
      try (InputStream inputStream = new FileInputStream(encodedVideoPath)) {
        videoS3Resource = s3Operations.upload(bucket, originalKey, inputStream,
            ObjectMetadata.builder().contentType(contentType).build());
      }
      // 임시 파일 삭제
      new File(tmpPath).delete();
    } else {
      // MOV가 아닌 경우 원본을 그대로 사용
      try (InputStream inputStream = new URL(tempVideoUrl).openStream()) {
        videoS3Resource = s3Operations.upload(bucket, originalKey, inputStream,
            ObjectMetadata.builder().contentType(contentType).build());
      }
    }

    return videoS3Resource.getURL().toExternalForm();
  }

}
