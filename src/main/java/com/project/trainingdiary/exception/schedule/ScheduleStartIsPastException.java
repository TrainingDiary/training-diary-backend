package com.project.trainingdiary.exception.schedule;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class ScheduleStartIsPastException extends GlobalException {

  public ScheduleStartIsPastException() {
    super(HttpStatus.CONFLICT, "과거의 일정은 예약할 수 없습니다.");
  }
}
