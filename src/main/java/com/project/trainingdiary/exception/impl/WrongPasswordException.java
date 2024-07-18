package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class WrongPasswordException extends
    GlobalException {

  public WrongPasswordException() {
    super(HttpStatus.valueOf(474), "비밀번호가 일치하지 않습니다.");
  }
}
