package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class VerificationCodeNotYetVerifiedException extends GlobalException {

  public VerificationCodeNotYetVerifiedException() {
    super(HttpStatus.BAD_REQUEST, "인증 코드가 아직 검증되지 않았습니다.");
  }
}
