package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class ScheduleStatusNotOpenException extends GlobalException {

  public ScheduleStatusNotOpenException() {
    super(HttpStatus.CONFLICT, "일정이 OPEN 상태가 아닙니다.");
  }
}
