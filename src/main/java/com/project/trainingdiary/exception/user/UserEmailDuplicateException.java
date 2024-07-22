package com.project.trainingdiary.exception.user;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class UserEmailDuplicateException extends GlobalException {

  public UserEmailDuplicateException() {
    super(HttpStatus.CONFLICT, "이미 등록된 이메일입니다");
  }
}
