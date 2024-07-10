package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class ScheduleStartTooSoon extends GlobalException {

  public ScheduleStartTooSoon() {
    super(HttpStatus.BAD_REQUEST, "1시간 내 시작하는 일정은 예약할 수 없습니다.");
  }
}
