package com.project.trainingdiary.exception.user;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class WrongPasswordException extends
    GlobalException {

  public WrongPasswordException() {
    super(HttpStatus.FORBIDDEN, "비밀번호가 틀렸습니다.");
  }
}
