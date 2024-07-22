package com.project.trainingdiary.exception.workout;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class WorkoutSessionAccessDeniedException extends GlobalException {

  public WorkoutSessionAccessDeniedException() {
    super(HttpStatus.NOT_FOUND, "해당 일지를 열람할 수 없습니다.");
  }
}
