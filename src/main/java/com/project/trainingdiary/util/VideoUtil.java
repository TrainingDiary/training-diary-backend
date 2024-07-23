package com.project.trainingdiary.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class VideoUtil {

  private static final String VIDEO_QUALITY = "1280:720";
  private static final String VIDEO_THUMBNAIL = "360:-1";

  public static void encodeVideo(InputStream inputStream, OutputStream outputStream)
      throws IOException, InterruptedException {
    ProcessBuilder processBuilder = new ProcessBuilder(
        "ffmpeg",
        "-i", "pipe:0",
        "-vf", "scale=" + VIDEO_QUALITY,
        "-preset", "slow",
        "pipe:1"
    );
    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
    Process process = processBuilder.start();

    // 입력 스트림 처리
    try (OutputStream processInput = process.getOutputStream()) {
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        processInput.write(buffer, 0, bytesRead);
      }
    }

    // 출력 스트림 처리
    try (InputStream processOutput = process.getInputStream()) {
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = processOutput.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
    }

    process.waitFor();
  }

  public static void generateThumbnail(InputStream inputStream, OutputStream outputStream)
      throws IOException, InterruptedException {
    ProcessBuilder processBuilder = new ProcessBuilder(
        "ffmpeg",
        "-i", "pipe:0",
        "-ss", "00:00:01.000",
        "-vframes", "1",
        "-vf", "scale=" + VIDEO_THUMBNAIL,
        "pipe:1"
    );
    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
    Process process = processBuilder.start();

    // 입력 스트림 처리
    try (OutputStream processInput = process.getOutputStream()) {
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        processInput.write(buffer, 0, bytesRead);
      }
    }

    // 출력 스트림 처리
    try (InputStream processOutput = process.getInputStream()) {
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = processOutput.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
    }

    process.waitFor();
  }

}
