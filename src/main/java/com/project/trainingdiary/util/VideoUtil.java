package com.project.trainingdiary.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class VideoUtil {

  private static final String VIDEO_QUALITY_WIDTH = "1280:-1";
  private static final String VIDEO_QUALITY_HEIGHT = "-1:1280";
  private static final String VIDEO_THUMBNAIL_WIDTH = "360:-1";
  private static final String VIDEO_THUMBNAIL_HEIGHT = "-1:360";

  public static boolean isVerticalVideo(String inputUrl) throws IOException, InterruptedException {
    ProcessBuilder processBuilder = new ProcessBuilder(
        "ffprobe",
        "-v", "error",
        "-select_streams", "v:0",
        "-show_entries", "stream=width,height",
        "-of", "default=noprint_wrappers=1:nokey=1",
        inputUrl
    );

    Process process = processBuilder.start();
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String widthLine = reader.readLine();
    String heightLine = reader.readLine();
    process.waitFor();

    if (widthLine != null && heightLine != null) {
      int width = Integer.parseInt(widthLine);
      int height = Integer.parseInt(heightLine);
      return height > width;
    }

    return false;
  }

  public static String encodeVideo(String inputUrl, String outputUrl)
      throws IOException, InterruptedException {
    boolean isVertical = isVerticalVideo(inputUrl);
    ProcessBuilder processBuilder;

    if (isVertical) {
      processBuilder = new ProcessBuilder(
          "ffmpeg",
          "-i", inputUrl,
          "-vf", "scale=" + VIDEO_QUALITY_HEIGHT,
          "-preset", "medium",
          outputUrl
      );
    } else {
      processBuilder = new ProcessBuilder(
          "ffmpeg",
          "-i", inputUrl,
          "-vf", "scale=" + VIDEO_QUALITY_WIDTH,
          "-preset", "medium",
          outputUrl
      );
    }

    Process process = processBuilder.start();
    process.waitFor();

    return outputUrl;
  }

  public static String generateThumbnail(String inputUrl, String outputUrl)
      throws IOException, InterruptedException {
    boolean isVertical = isVerticalVideo(inputUrl);
    ProcessBuilder processBuilder;

    if (isVertical) {
      processBuilder = new ProcessBuilder(
          "ffmpeg",
          "-i", inputUrl,
          "-ss", "00:00:01.000",
          "-vframes", "1",
          "-vf", "scale=" + VIDEO_THUMBNAIL_WIDTH,
          "-threads", "4",
          outputUrl
      );
    } else {
      processBuilder = new ProcessBuilder(
          "ffmpeg",
          "-i", inputUrl,
          "-ss", "00:00:01.000",
          "-vframes", "1",
          "-vf", "scale=" + VIDEO_THUMBNAIL_HEIGHT,
          "-threads", "4",
          outputUrl
      );
    }

    Process process = processBuilder.start();
    process.waitFor();

    return outputUrl;
  }

}
