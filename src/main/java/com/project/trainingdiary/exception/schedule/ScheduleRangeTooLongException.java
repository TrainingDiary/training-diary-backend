package com.project.trainingdiary.exception.schedule;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class ScheduleRangeTooLongException extends GlobalException {

  public ScheduleRangeTooLongException() {
    super(HttpStatus.BAD_REQUEST, "일정 간격이 너무 깁니다.");
  }
}
