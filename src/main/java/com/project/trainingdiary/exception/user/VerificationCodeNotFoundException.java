package com.project.trainingdiary.exception.user;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class VerificationCodeNotFoundException extends GlobalException {

  public VerificationCodeNotFoundException() {
    super(HttpStatus.NOT_FOUND, "인증번호가 존재하지 않습니다.");
  }
}
