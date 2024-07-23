package com.project.trainingdiary.util;

import com.project.trainingdiary.exception.workout.FileNoNameException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;
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

  public static BufferedImage resizeImageToWidth(BufferedImage originalImage, int width)
      throws IOException {
    return Thumbnails.of(originalImage) // thumbnailator 사용
        .width(width)
        .keepAspectRatio(true)
        .asBufferedImage();
    //return Scalr.resize(originalImage, QUALITY, FIT_TO_WIDTH, width);   // scalr 사용
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
