package com.project.trainingdiary.exception.workout;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class WorkoutSessionNotFoundException extends GlobalException {

  public WorkoutSessionNotFoundException(Long id) {
    super(HttpStatus.NOT_FOUND, "해당 일련번호의 운동 일지를 찾을 수 없습니다. ID: " + id);
  }
}
