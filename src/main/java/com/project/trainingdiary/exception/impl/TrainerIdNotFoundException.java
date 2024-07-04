package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class TrainerIdNotFoundException extends GlobalException {

  public TrainerIdNotFoundException(Long id) {
    super(HttpStatus.NOT_FOUND, "해당 일련번호의 트레이너를 찾을 수 없습니다. ID: " + id);
  }
}
