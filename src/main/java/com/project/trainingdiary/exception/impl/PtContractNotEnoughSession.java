package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class PtContractNotEnoughSession extends GlobalException {

  public PtContractNotEnoughSession() {
    super(HttpStatus.BAD_REQUEST, "PT 횟수가 부족합니다.");
  }
}
