package com.project.trainingdiary.util;

import static org.imgscalr.Scalr.Method.QUALITY;
import static org.imgscalr.Scalr.Mode.AUTOMATIC;

import com.project.trainingdiary.exception.impl.FileNoNameException;
import com.project.trainingdiary.exception.impl.InvalidFileTypeException;
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
import org.imgscalr.Scalr;
import org.springframework.web.multipart.MultipartFile;

public class ImageUtil {


  private static final int THUMBNAIL_WIDTH = 150;
  private static final int THUMBNAIL_HEIGHT = 150;

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
      MultipartFile file,
      String originalKey
  ) throws IOException {
    try (InputStream inputStream = file.getInputStream()) {
      return s3Operations.upload(bucket, originalKey, inputStream,
          ObjectMetadata.builder().contentType(file.getContentType()).build());
    }
  }

  private static String createAndUploadThumbnail(
      S3Operations s3Operations,
      String bucket,
      MultipartFile file,
      String originalKey
  ) throws IOException {
    BufferedImage originalImage = ImageIO.read(file.getInputStream());
    BufferedImage thumbnailImage = Scalr
        .resize(originalImage, QUALITY, AUTOMATIC, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);

    String extension = getExtension(checkFileNameExist(file));

    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
      ImageIO.write(thumbnailImage, extension, byteArrayOutputStream);
      try (InputStream inputStream = new ByteArrayInputStream(
          byteArrayOutputStream.toByteArray())) {
        String thumbnailKey = "thumb_" + originalKey;
        String contentType = "image/" + extension;
        S3Resource s3Resource = s3Operations.upload(bucket, thumbnailKey, inputStream,
            ObjectMetadata.builder().contentType(contentType).build());
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

  public static UploadResult uploadImageAndThumbnail(S3Operations s3Operations, String bucket,
      MultipartFile file) throws IOException {
    if (!isValidImageType(file)) {
      throw new InvalidFileTypeException();
    }
    String extension = getExtension(checkFileNameExist(file));
    String originalKey = UUID.randomUUID() + "." + extension;
    S3Resource s3Resource = uploadImage(s3Operations, bucket, file, originalKey);
    String originalUrl = s3Resource.getURL().toExternalForm();
    String thumbnailUrl = createAndUploadThumbnail(s3Operations, bucket, file, originalKey);
    return new UploadResult(originalUrl, thumbnailUrl);
  }

  @Getter
  @AllArgsConstructor
  public static class UploadResult {

    private final String originalUrl;
    private final String thumbnailUrl;
  }

}
