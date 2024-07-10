package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class ScheduleStartIsPast extends GlobalException {

  public ScheduleStartIsPast() {
    super(HttpStatus.BAD_REQUEST, "과거의 일정은 예약할 수 없습니다.");
  }
}
