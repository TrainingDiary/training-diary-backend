package com.project.trainingdiary.exception.schedule;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class ScheduleStartWithin1DayException extends GlobalException {

  public ScheduleStartWithin1DayException() {
    super(HttpStatus.EXPECTATION_FAILED, "일정이 하루 안에 시작되므로 취소할 수 없습니다.");
  }
}
