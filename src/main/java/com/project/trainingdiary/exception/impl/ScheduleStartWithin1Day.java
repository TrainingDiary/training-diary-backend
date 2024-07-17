package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class ScheduleStartWithin1Day extends GlobalException {

  public ScheduleStartWithin1Day() {
    super(HttpStatus.BAD_REQUEST, "스케쥴이 하루 안에 시작되므로 취소할 수 없습니다.");
  }
}
