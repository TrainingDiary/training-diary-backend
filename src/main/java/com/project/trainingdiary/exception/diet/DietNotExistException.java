package com.project.trainingdiary.exception.diet;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class DietNotExistException extends GlobalException {

  public DietNotExistException() {
    super(HttpStatus.NOT_FOUND, "해당 식단을 찾을 수 없습니다.");
  }
}
