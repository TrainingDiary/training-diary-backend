package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class PasswordMismatchedException extends GlobalException {

  public PasswordMismatchedException() {
    super(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
  }
}
