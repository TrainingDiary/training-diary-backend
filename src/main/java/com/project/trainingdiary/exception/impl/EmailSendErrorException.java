package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class EmailSendErrorException extends GlobalException {

  public EmailSendErrorException() {
    super(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 보내는데 오류가 생겼습니다.");
  }
}
