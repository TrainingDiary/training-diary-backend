package com.project.trainingdiary.exception.workout;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class WorkoutNotFoundException extends GlobalException {

  public WorkoutNotFoundException(Long id) {
    super(HttpStatus.NOT_FOUND, "해당 일련번호의 운동 상세 기록을 찾을 수 없습니다. ID: " + id);
  }
}
