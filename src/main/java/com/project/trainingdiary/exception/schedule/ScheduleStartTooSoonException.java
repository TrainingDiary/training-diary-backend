package com.project.trainingdiary.exception.schedule;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class ScheduleStartTooSoonException extends GlobalException {

  public ScheduleStartTooSoonException() {
    super(HttpStatus.EXPECTATION_FAILED, "1시간 내 시작하는 일정은 예약할 수 없습니다.");
  }
}
