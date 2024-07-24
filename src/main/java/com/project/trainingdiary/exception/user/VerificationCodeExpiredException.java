package com.project.trainingdiary.exception.user;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class VerificationCodeExpiredException extends GlobalException {

  public VerificationCodeExpiredException() {
    super(HttpStatus.NOT_ACCEPTABLE, "인증 코드가 만료 되었습니다.");
  }
}
