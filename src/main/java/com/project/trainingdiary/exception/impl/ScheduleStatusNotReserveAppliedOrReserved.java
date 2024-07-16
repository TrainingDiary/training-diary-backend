package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class ScheduleStatusNotReserveAppliedOrReserved extends GlobalException {

  public ScheduleStatusNotReserveAppliedOrReserved() {
    super(HttpStatus.BAD_REQUEST, "일정의 상태가 RESERVED나 RESERVE_APPLIED가 아닙니다.");
  }
}
