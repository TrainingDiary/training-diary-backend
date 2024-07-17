package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class InvalidUserRoleTypeException extends GlobalException {

  public InvalidUserRoleTypeException() {
    super(HttpStatus.FORBIDDEN, "유효하지 않은 role 입니다.");
  }
}
