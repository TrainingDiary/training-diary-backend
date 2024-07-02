package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class VerificationCodeExpiredException extends
    GlobalException {

  public VerificationCodeExpiredException() {
    super(HttpStatus.BAD_REQUEST, "Verification code expired.");
  }
}
