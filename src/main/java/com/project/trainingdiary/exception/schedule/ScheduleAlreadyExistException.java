package com.project.trainingdiary.exception.schedule;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class ScheduleAlreadyExistException extends GlobalException {

  public ScheduleAlreadyExistException() {
    super(HttpStatus.BAD_REQUEST, "같은 시간에 이미 일정이 존재합니다.");
  }
}
