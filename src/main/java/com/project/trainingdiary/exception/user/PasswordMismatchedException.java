package com.project.trainingdiary.exception.user;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class PasswordMismatchedException extends GlobalException {

  public PasswordMismatchedException() {
    super(HttpStatus.FORBIDDEN, "비밀번호가 일치하지 않습니다.");
  }
}
