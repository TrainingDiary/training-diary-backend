package com.project.trainingdiary.provider;

import static com.project.trainingdiary.util.MediaUtil.getExtension;
import static com.project.trainingdiary.util.MediaUtil.getMediaType;

import com.project.trainingdiary.util.MediaUtil;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Component
public class S3DietImageProvider {

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucket;

  private final S3Operations s3Operations;

  public String uploadImageToS3(MultipartFile file) throws IOException {
    BufferedImage originalImage = ImageIO.read(file.getInputStream());
    BufferedImage resizedImage = MediaUtil.resizeOriginalImage(originalImage);
    String extension = getExtension(MediaUtil.checkFileNameExist(file));
    String key = UUID.randomUUID() + "." + extension;

    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ImageIO.write(resizedImage, extension, baos);
      try (InputStream inputStream = new ByteArrayInputStream(baos.toByteArray())) {
        S3Resource s3Resource = s3Operations.upload(bucket, key, inputStream,
            ObjectMetadata.builder().contentType(file.getContentType()).build());
        return s3Resource.getURL().toExternalForm();
      }
    }
  }

  public String uploadThumbnailToS3(MultipartFile file, String originalKey, String extension)
      throws IOException {
    BufferedImage originalImage = ImageIO.read(file.getInputStream());
    BufferedImage thumbnailImage = MediaUtil.resizeThumbnail(originalImage);

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

  public void deleteFileFromS3(String fileUrl) {
    String fileKey = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    s3Operations.deleteObject(bucket, fileKey);
  }
}
