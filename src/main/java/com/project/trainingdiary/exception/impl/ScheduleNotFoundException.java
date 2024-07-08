package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class ScheduleNotFoundException extends GlobalException {

  public ScheduleNotFoundException() {
    super(HttpStatus.NOT_FOUND, "스케쥴이 없습니다.");
  }
}
