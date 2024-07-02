package com.project.trainingdiary.model;

public class VerificationNumberGenerator {

  public static String generateVerificationNumber() {

    StringBuilder verificationNumber = new StringBuilder();

    for (int i = 0; i < 6; i++) {
      verificationNumber.append((int) (Math.random() * 10));
    }
    return verificationNumber.toString();
  }
}
