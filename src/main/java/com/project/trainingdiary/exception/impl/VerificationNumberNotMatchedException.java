package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class VerificationNumberNotMatchedException extends GlobalException {

  public VerificationNumberNotMatchedException() {
    super(HttpStatus.BAD_REQUEST, "Verification number not matched.");
  }
}
