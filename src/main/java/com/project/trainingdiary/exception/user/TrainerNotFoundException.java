package com.project.trainingdiary.exception.user;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class TrainerNotFoundException extends GlobalException {

  public TrainerNotFoundException() {
    super(HttpStatus.NOT_FOUND, "트레이너가 존재하지 않습니다.");
  }
}
