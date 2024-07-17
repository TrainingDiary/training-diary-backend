package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class InvalidFileTypeException extends GlobalException {

  public InvalidFileTypeException() {
    super(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "유효하지 않은 미디어 타입 입니다.");
  }
}
