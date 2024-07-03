package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class VerificationCodeNotMatchedException extends GlobalException {

  public VerificationCodeNotMatchedException() {
    super(HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다.");
  }
}
