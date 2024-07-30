package com.project.trainingdiary.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.springframework.web.multipart.MultipartFile;

public class VideoUtil {

  private static final String VIDEO_QUALITY_WIDTH = "1280:-1";
  private static final String VIDEO_QUALITY_HEIGHT = "-1:1280";
  private static final String VIDEO_THUMBNAIL_WIDTH = "360:-1";
  private static final String VIDEO_THUMBNAIL_HEIGHT = "-1:360";

  public static boolean isVerticalVideo(MultipartFile file)
      throws IOException, InterruptedException {
    File tempFile = File.createTempFile("video", ".tmp");

    try (InputStream inputStream = file.getInputStream()) {
      try (OutputStream outputStream = new FileOutputStream(tempFile)) {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
      }
    }

    ProcessBuilder processBuilder = new ProcessBuilder(
        "ffprobe",
        "-v", "error",
        "-select_streams", "v:0",
        "-show_entries", "stream=width,height",
        "-of", "default=noprint_wrappers=1:nokey=1",
        tempFile.getAbsolutePath()
    );

    Process process = processBuilder.start();
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String widthLine = reader.readLine();
    String heightLine = reader.readLine();
    process.waitFor();

    tempFile.delete();

    if (widthLine != null && heightLine != null) {
      int width = Integer.parseInt(widthLine);
      int height = Integer.parseInt(heightLine);
      return height > width;
    }

    return false;
  }

  public static String encodeVideo(MultipartFile file, String outputUrl)
      throws IOException, InterruptedException {
    boolean isVertical = isVerticalVideo(file);

    ProcessBuilder processBuilder;
    File tempFile = File.createTempFile("video", ".tmp");

    try (InputStream inputStream = file.getInputStream()) {
      try (OutputStream outputStream = new FileOutputStream(tempFile)) {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
      }
    }

    if (isVertical) {
      processBuilder = new ProcessBuilder(
          "ffmpeg",
          "-i", tempFile.getAbsolutePath(),
          "-vf", "scale=" + VIDEO_QUALITY_HEIGHT,
          "-preset", "medium",
          outputUrl
      );
    } else {
      processBuilder = new ProcessBuilder(
          "ffmpeg",
          "-i", tempFile.getAbsolutePath(),
          "-vf", "scale=" + VIDEO_QUALITY_WIDTH,
          "-preset", "medium",
          outputUrl
      );
    }

    Process process = processBuilder.start();
    process.waitFor();

    tempFile.delete();

    return outputUrl;

  }

  public static String generateThumbnail(MultipartFile file, String outputUrl)
      throws IOException, InterruptedException {
    boolean isVertical = isVerticalVideo(file);

    ProcessBuilder processBuilder;
    File tempFile = File.createTempFile("video", ".tmp");

    try (InputStream inputStream = file.getInputStream()) {
      try (OutputStream outputStream = new FileOutputStream(tempFile)) {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
      }
    }

    if (isVertical) {
      processBuilder = new ProcessBuilder(
          "ffmpeg",
          "-i", tempFile.getAbsolutePath(),
          "-ss", "00:00:01.000",
          "-vframes", "1",
          "-vf", "scale=" + VIDEO_THUMBNAIL_WIDTH,
          "-threads", "4",
          outputUrl
      );
    } else {
      processBuilder = new ProcessBuilder(
          "ffmpeg",
          "-i", tempFile.getAbsolutePath(),
          "-ss", "00:00:01.000",
          "-vframes", "1",
          "-vf", "scale=" + VIDEO_THUMBNAIL_HEIGHT,
          "-threads", "4",
          outputUrl
      );
    }

    Process process = processBuilder.start();
    process.waitFor();

    tempFile.delete();

    return outputUrl;
  }

}
