package com.project.trainingdiary.util;

import com.project.trainingdiary.exception.workout.FileNoNameException;
import com.project.trainingdiary.exception.workout.InvalidFileTypeException;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

public class WorkoutImageUtil {

  private static final int THUMBNAIL_SIZE = 250;
  private static final int ORIGINAL_SIZE = 360;

  private static boolean isValidImageType(MultipartFile file) {
    String contentType = file.getContentType();
    return contentType != null && contentType.startsWith("image/");
  }

  public static String getExtension(String filename) {
    return filename.substring(filename.lastIndexOf('.') + 1);
  }

  private static S3Resource uploadImage(
      S3Operations s3Operations,
      String bucket,
      String key,
      InputStream inputStream,
      String contentType
  ) {
    return s3Operations.upload(bucket, key, inputStream,
        ObjectMetadata.builder().contentType(contentType).build());
  }

  private static BufferedImage resizeImageToWidth(
      BufferedImage originalImage,
      int width
  ) throws IOException {
    return Thumbnails.of(originalImage)     // thumbnailator 사용
        .width(width)
        .keepAspectRatio(true)
        .asBufferedImage();
    //return Scalr.resize(originalImage, QUALITY, FIT_TO_WIDTH, width);   // scalr 사용
  }

  private static String uploadResizedImage(
      S3Operations s3Operations,
      String bucket,
      MultipartFile file,
      String key,
      BufferedImage resizedImage
  ) throws IOException {
    String extension = getExtension(checkFileNameExist(file));

    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
      ImageIO.write(resizedImage, extension, byteArrayOutputStream);
      try (InputStream inputStream = new ByteArrayInputStream(
          byteArrayOutputStream.toByteArray())
      ) {
        String contentType = "image/" + extension;
        S3Resource s3Resource = uploadImage(s3Operations, bucket, key, inputStream, contentType);
        return s3Resource.getURL().toExternalForm();
      }
    }
  }

  public static String checkFileNameExist(MultipartFile file) {
    String filename = file.getOriginalFilename();
    if (filename == null) {
      throw new FileNoNameException();
    }
    return filename;
  }

  public static UploadResult uploadImageAndThumbnail(
      S3Operations s3Operations,
      String bucket,
      MultipartFile file
  ) throws IOException {
    if (!isValidImageType(file)) {
      throw new InvalidFileTypeException();
    }
    String extension = getExtension(checkFileNameExist(file));
    String key = UUID.randomUUID() + "." + extension;

    BufferedImage originalImage = ImageIO.read(file.getInputStream());

    // 썸네일 생성 및 업로드
    BufferedImage thumbnailImage = resizeImageToWidth(originalImage, THUMBNAIL_SIZE);
    String thumbnailUrl = uploadResizedImage(
        s3Operations, bucket, file, "thumb_" + key, thumbnailImage);

    // 원본 크기 조정 이미지 생성 및 업로드
    BufferedImage resizedOriginalImage = resizeImageToWidth(originalImage, ORIGINAL_SIZE);
    String originalUrl = uploadResizedImage(
        s3Operations, bucket, file, "original_" + key, resizedOriginalImage);

    return new UploadResult(originalUrl, thumbnailUrl);
  }

  @Getter
  @AllArgsConstructor
  public static class UploadResult {

    private final String originalUrl;
    private final String thumbnailUrl;
  }

}
