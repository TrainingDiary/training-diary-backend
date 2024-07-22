package com.project.trainingdiary.exception.user;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class InvalidUserRoleTypeException extends GlobalException {

  public InvalidUserRoleTypeException() {
    super(HttpStatus.NOT_FOUND, "해당 일지를 찾을 수 없습니다.");
  }
}
