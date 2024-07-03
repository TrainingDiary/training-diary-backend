package com.project.trainingdiary.util;

public class VerificationCodeGeneratorUtil {

  public static String generateVerificationCode() {

    StringBuilder verificationCode = new StringBuilder();

    for (int i = 0; i < 6; i++) {
      verificationCode.append((int) (Math.random() * 10));
    }
    return verificationCode.toString();
  }
}
