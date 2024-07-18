package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class VerificationCodeExpiredException extends GlobalException {

  public VerificationCodeExpiredException() {
    super(HttpStatus.valueOf(472), "인증 코드가 만료 되었습니다.");
  }
}
