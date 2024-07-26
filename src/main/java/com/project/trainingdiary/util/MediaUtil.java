package com.project.trainingdiary.util;

import static org.imgscalr.Scalr.Method.QUALITY;
import static org.imgscalr.Scalr.Mode.FIT_TO_HEIGHT;
import static org.imgscalr.Scalr.Mode.FIT_TO_WIDTH;

import com.project.trainingdiary.exception.workout.FileNoNameException;
import java.awt.image.BufferedImage;
import org.imgscalr.Scalr;
import org.springframework.web.multipart.MultipartFile;

public class MediaUtil {

  public static boolean isValidImageType(MultipartFile file) {
    return file.getContentType() != null && file.getContentType().startsWith("image/");
  }

  public static boolean isValidVideoType(MultipartFile file) {
    return file.getContentType() != null && file.getContentType().startsWith("video/");
  }

  public static String getExtension(String filename) {
    return filename.substring(filename.lastIndexOf('.') + 1);
  }

  public static BufferedImage resizeImageToWidth(BufferedImage originalImage, int targetSize) {
    int originalWidth = originalImage.getWidth();
    int originalHeight = originalImage.getHeight();

    if (originalWidth <= originalHeight) {
      return Scalr.resize(originalImage, QUALITY, FIT_TO_WIDTH, targetSize);
    } else {
      return Scalr.resize(originalImage, QUALITY, FIT_TO_HEIGHT, targetSize);
    }
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

}
