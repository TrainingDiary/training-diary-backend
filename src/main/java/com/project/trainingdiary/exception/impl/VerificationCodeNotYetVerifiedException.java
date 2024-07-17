package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class VerificationCodeNotYetVerifiedException extends GlobalException {

  public VerificationCodeNotYetVerifiedException() {
    super(HttpStatus.BAD_REQUEST, "아직 인증 처리를 안했습니다.");
  }
}
