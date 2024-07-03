package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class TrainerEmailDuplicateException extends GlobalException {

  public TrainerEmailDuplicateException() {
    super(HttpStatus.CONFLICT, "이미 가입된 회원입니다 (Trainer).");
  }
}
