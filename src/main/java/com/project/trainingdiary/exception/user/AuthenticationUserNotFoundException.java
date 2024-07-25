package com.project.trainingdiary.exception.user;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class AuthenticationUserNotFoundException extends GlobalException {

  public AuthenticationUserNotFoundException() {
    super(HttpStatus.BAD_REQUEST, "가입된 이메일이 아닙니다.");
  }
}
