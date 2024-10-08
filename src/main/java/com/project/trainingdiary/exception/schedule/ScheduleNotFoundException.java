package com.project.trainingdiary.exception.schedule;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class ScheduleNotFoundException extends GlobalException {

  public ScheduleNotFoundException() {
    super(HttpStatus.NOT_FOUND, "일정이 없습니다.");
  }
}
