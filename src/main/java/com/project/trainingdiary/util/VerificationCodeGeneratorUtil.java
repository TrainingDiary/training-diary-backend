package com.project.trainingdiary.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class VerificationCodeGeneratorUtil {

  private static final int LENGTH_OF_CODE = 6;

  public static String generateVerificationCode() {

    StringBuilder verificationCode = new StringBuilder();

    for (int i = 1; i <= LENGTH_OF_CODE; i++) {
      verificationCode.append((int) (Math.random() * 10));
    }
    return verificationCode.toString();
  }

  public static String generateExpirationTime() {
    LocalDateTime expirationDateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(10);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    return expirationDateTime.format(formatter);
  }
}
