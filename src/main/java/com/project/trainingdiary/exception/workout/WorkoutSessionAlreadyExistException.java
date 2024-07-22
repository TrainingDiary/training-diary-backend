package com.project.trainingdiary.exception.workout;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class WorkoutSessionAlreadyExistException extends GlobalException {

  public WorkoutSessionAlreadyExistException(int sessionNumber) {
    super(HttpStatus.CONFLICT, "이미 해당 PT 수업에 대한 일지가 존재합니다. session number: " + sessionNumber);
  }
}
