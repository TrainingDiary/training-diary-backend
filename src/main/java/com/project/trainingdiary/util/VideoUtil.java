package com.project.trainingdiary.util;

import java.io.IOException;

public class VideoUtil {

  private static final String VIDEO_QUALITY = "1280:720";
  private static final String VIDEO_THUMBNAIL = "360:-1";

  public static String encodeVideo(String inputUrl, String outputUrl)
      throws IOException, InterruptedException {
    ProcessBuilder processBuilder = new ProcessBuilder(
        "ffmpeg",
        "-i", inputUrl,
        "-vf", "scale=" + VIDEO_QUALITY,
        "-preset", "slow",
        outputUrl
    );

    Process process = processBuilder.start();
    process.waitFor();

    return outputUrl;
  }

  public static String generateThumbnail(String inputUrl, String outputUrl)
      throws IOException, InterruptedException {
    ProcessBuilder processBuilder = new ProcessBuilder(
        "ffmpeg",
        "-i", inputUrl,
        "-ss", "00:00:01.000",
        "-vframes", "1",
        "-vf", "scale=" + VIDEO_THUMBNAIL,
        outputUrl
    );

    Process process = processBuilder.start();
    process.waitFor();

    return outputUrl;
  }

}
