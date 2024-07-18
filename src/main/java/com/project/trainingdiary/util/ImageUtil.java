package com.project.trainingdiary.util;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Resource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ImageUtil {

  private final S3Operations s3Operations;

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucket;

  private static final int THUMBNAIL_WIDTH = 150;
  private static final int THUMBNAIL_HEIGHT = 150;

  public String uploadImageToS3(MultipartFile file) throws IOException {
    String extension = getExtension(file.getOriginalFilename());
    String key = UUID.randomUUID() + "." + extension;

    try (InputStream inputStream = file.getInputStream()) {
      S3Resource s3Resource = s3Operations.upload(bucket, key, inputStream,
          ObjectMetadata.builder().contentType(file.getContentType()).build());
      return s3Resource.getURL().toExternalForm();
    }
  }

  public String createAndUploadThumbnail(MultipartFile file, String originalKey, String extension)
      throws IOException {
    BufferedImage originalImage = ImageIO.read(file.getInputStream());
    BufferedImage thumbnailImage = Scalr
        .resize(originalImage, Method.QUALITY, Mode.AUTOMATIC, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);

    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
      ImageIO.write(thumbnailImage, extension, byteArrayOutputStream);
      try (InputStream inputStream = new ByteArrayInputStream(
          byteArrayOutputStream.toByteArray())) {
        String thumbnailKey = "thumb_" + originalKey.substring(originalKey.lastIndexOf("/") + 1);
        S3Resource s3Resource = s3Operations.upload(bucket, thumbnailKey, inputStream,
            ObjectMetadata.builder().contentType(getMediaType(extension)).build());
        return s3Resource.getURL().toExternalForm();
      }
    }
  }

  public boolean isValidImageType(MultipartFile file) {
    return MediaType.IMAGE_JPEG.toString().equals(file.getContentType()) ||
        MediaType.IMAGE_PNG.toString().equals(file.getContentType());
  }

  public String getExtension(String filename) {
    if (filename == null) {
      return "";
    }
    int dotIndex = filename.lastIndexOf('.');
    return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
  }

  private String getMediaType(String extension) {
    if ("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension)) {
      return MediaType.IMAGE_JPEG_VALUE;
    } else if ("png".equalsIgnoreCase(extension)) {
      return MediaType.IMAGE_PNG_VALUE;
    }
    return MediaType.APPLICATION_OCTET_STREAM_VALUE;
  }
}