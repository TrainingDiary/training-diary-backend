package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class WorkoutSessionAccessDeniedException extends GlobalException {

  public WorkoutSessionAccessDeniedException() {
    super(HttpStatus.FORBIDDEN, "해당 일지에 대해 접근 권한이 없습니다.");
  }
}
