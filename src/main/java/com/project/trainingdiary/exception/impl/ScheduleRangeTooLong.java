package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class ScheduleRangeTooLong extends GlobalException {

  public ScheduleRangeTooLong() {
    super(HttpStatus.PAYLOAD_TOO_LARGE, "일정 간격이 너무 깁니다.");
  }
}
