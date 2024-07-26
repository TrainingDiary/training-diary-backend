package com.project.trainingdiary.provider;

import com.project.trainingdiary.exception.workout.InvalidFileTypeException;
import com.project.trainingdiary.util.MediaUtil;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Resource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class S3ImageProvider {

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucket;

  private final S3Operations s3Operations;

  public String uploadImage(MultipartFile file, String key, String extension, int targetSize)
      throws IOException {
    if (!MediaUtil.isValidImageType(file)) {
      throw new InvalidFileTypeException();
    }

    BufferedImage originalImage = ImageIO.read(file.getInputStream());

    // 이미지 리사이즈
    BufferedImage resizedImage = MediaUtil.resizeImageToWidth(originalImage, targetSize);

    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
      ImageIO.write(resizedImage, extension, byteArrayOutputStream);
      try (InputStream inputStream = new ByteArrayInputStream(
          byteArrayOutputStream.toByteArray())) {
        String contentType = "image/" + extension;
        S3Resource s3Resource = s3Operations.upload(bucket, key, inputStream,
            ObjectMetadata.builder().contentType(contentType).build());
        return s3Resource.getURL().toExternalForm();
      }
    }
  }

  public void deleteMedia(String mediaUrl) {
    String key = MediaUtil.extractKey(mediaUrl);
    s3Operations.deleteObject(bucket, key);
  }

}
