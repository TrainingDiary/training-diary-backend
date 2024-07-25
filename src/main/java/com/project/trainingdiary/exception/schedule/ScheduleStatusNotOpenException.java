package com.project.trainingdiary.exception.schedule;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class ScheduleStatusNotOpenException extends GlobalException {

  public ScheduleStatusNotOpenException() {
    super(HttpStatus.BAD_REQUEST, "일정이 OPEN 상태가 아닙니다.");
  }
}
