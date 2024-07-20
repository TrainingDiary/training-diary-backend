package com.project.trainingdiary.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class VerificationCodeGeneratorUtil {

  public static String generateVerificationCode() {

    StringBuilder verificationCode = new StringBuilder();

    for (int i = 0; i < 6; i++) {
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
