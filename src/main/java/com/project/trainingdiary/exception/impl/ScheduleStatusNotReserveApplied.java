package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class ScheduleStatusNotReserveApplied extends GlobalException {

  public ScheduleStatusNotReserveApplied() {
    super(HttpStatus.BAD_REQUEST, "일정의 상태가 RESERVE_APPLIED가 아닙니다.");
  }
}
