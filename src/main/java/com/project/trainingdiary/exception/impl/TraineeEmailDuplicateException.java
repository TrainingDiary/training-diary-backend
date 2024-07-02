package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class TraineeEmailDuplicateException extends GlobalException {

  public TraineeEmailDuplicateException() {
    super(HttpStatus.CONFLICT, "이미 가입된 회원입니다 (Trainee).");
  }
}
