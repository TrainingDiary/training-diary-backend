package com.project.trainingdiary.exception.user;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class TraineeNotFoundException extends GlobalException {

  public TraineeNotFoundException() {
    super(HttpStatus.NOT_FOUND, "트레이니가 존재하지 않습니다.");
  }
}
