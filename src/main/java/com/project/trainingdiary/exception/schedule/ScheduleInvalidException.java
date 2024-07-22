package com.project.trainingdiary.exception.schedule;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class ScheduleInvalidException extends GlobalException {

  public ScheduleInvalidException() {
    super(HttpStatus.BAD_REQUEST, "일정 형식이 맞지 않습니다.");
  }
}
