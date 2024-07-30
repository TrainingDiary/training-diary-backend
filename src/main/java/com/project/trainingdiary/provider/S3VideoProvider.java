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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class S3VideoProvider {

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucket;

  private final S3Operations s3Operations;

  public String uploadVideo(MultipartFile video, String uuid)
      throws IOException, InterruptedException {
    String extension = MediaUtil.getExtension(MediaUtil.checkFileNameExist(video));

    String originalKey =
        "original_" + uuid + "." + (extension.equalsIgnoreCase("mov") ? "mp4" : extension);

    String contentType = video.getContentType();
    if ("mov".equalsIgnoreCase(extension)) {
      contentType = "video/mp4";
    }

    return encodeAndUploadVideo(video, originalKey, contentType);
  }

  public String uploadThumbnail(MultipartFile video, String uuid)
      throws IOException, InterruptedException {
    String thumbnailKey = "thumb_" + uuid + ".png";

    String tmpPath = "/tmp/thumb_" + uuid + ".png";
    String thumbPath = VideoUtil.generateThumbnail(video, tmpPath);

    S3Resource thumbS3Resource;
    File tempFile = new File(thumbPath);

    try (InputStream inputStream = new FileInputStream(tempFile)) {
      thumbS3Resource = s3Operations.upload(bucket, thumbnailKey, inputStream,
          ObjectMetadata.builder().contentType("image/png").build());
    } finally {
      if (tempFile.exists()) {
        tempFile.delete();
      }
    }

    return thumbS3Resource.getURL().toExternalForm();
  }

  private String encodeAndUploadVideo(
      MultipartFile video,
      String originalKey,
      String contentType
  ) throws IOException, InterruptedException {

    String tmpPath = "/tmp/" + originalKey;

    File tempFile = null;
    S3Resource videoS3Resource;
    try {
      String encodedVideoPath = VideoUtil.encodeVideo(video, tmpPath);
      tempFile = new File(encodedVideoPath);
      try (InputStream inputStream = new FileInputStream(tempFile)) {
        videoS3Resource = s3Operations.upload(bucket, originalKey, inputStream,
            ObjectMetadata.builder().contentType(contentType).build());
      }
    } finally {
      if (tempFile != null && tempFile.exists()) {
        tempFile.delete();
      } else {
        new File(tmpPath).delete();
      }
    }

    return videoS3Resource.getURL().toExternalForm();
  }

}
