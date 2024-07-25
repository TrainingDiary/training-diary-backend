package com.project.trainingdiary.util;

import static org.imgscalr.Scalr.Method.QUALITY;
import static org.imgscalr.Scalr.Mode.FIT_TO_WIDTH;

import com.project.trainingdiary.exception.workout.FileNoNameException;
import java.awt.image.BufferedImage;
import org.imgscalr.Scalr;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

public class MediaUtil {

  private static final int THUMBNAIL_WIDTH = 150;
  private static final int THUMBNAIL_HEIGHT = 150;

  private static final int ORIGINAL_WIDTH = 410;
  private static final int ORIGINAL_HEIGHT = 410;

  public static boolean isValidImageType(MultipartFile file) {
    return file.getContentType() != null && file.getContentType().startsWith("image/");
  }

  public static boolean isValidVideoType(MultipartFile file) {
    return file.getContentType() != null && file.getContentType().startsWith("video/");
  }

  public static String getExtension(String filename) {
    return filename.substring(filename.lastIndexOf('.') + 1);
  }

  public static BufferedImage resizeImageToWidth(BufferedImage originalImage, int width) {
    return Scalr.resize(originalImage, QUALITY, FIT_TO_WIDTH, width);
  }

  public static BufferedImage resizeThumbnail(BufferedImage originalImage) {
    return Scalr.resize(originalImage, QUALITY, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
  }

  public static BufferedImage resizeOriginalImage(BufferedImage originalImage) {
    return Scalr.resize(originalImage, QUALITY, ORIGINAL_WIDTH, ORIGINAL_HEIGHT);
  }

  public static String checkFileNameExist(MultipartFile file) {
    String filename = file.getOriginalFilename();
    if (filename == null) {
      throw new FileNoNameException();
    }
    return filename;
  }

  public static String extractKey(String url) {
    return url.substring(url.lastIndexOf("/") + 1);
  }

  public static String getMediaType(String extension) {
    if ("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension)) {
      return MediaType.IMAGE_JPEG_VALUE;
    } else if ("png".equalsIgnoreCase(extension)) {
      return MediaType.IMAGE_PNG_VALUE;
    }
    return MediaType.APPLICATION_OCTET_STREAM_VALUE;
  }

}
