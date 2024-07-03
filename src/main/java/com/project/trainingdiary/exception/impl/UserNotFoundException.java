package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends GlobalException {

  public UserNotFoundException() {
    super(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다.");
  }
}
