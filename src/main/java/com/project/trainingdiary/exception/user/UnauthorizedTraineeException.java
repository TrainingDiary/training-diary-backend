package com.project.trainingdiary.exception.user;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class UnauthorizedTraineeException extends GlobalException {

  public UnauthorizedTraineeException() {
    super(HttpStatus.BAD_REQUEST, "다른 트레이니 정보를 볼 수 없습니다.");
  }
}
