package com.project.trainingdiary.exception.workout;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class FileNoNameException extends GlobalException {

  public FileNoNameException() {
    super(HttpStatus.UNPROCESSABLE_ENTITY, "파일은 파일 이름이 있어야 합니다.");
  }
}
