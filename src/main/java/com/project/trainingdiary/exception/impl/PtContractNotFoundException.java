package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class PtContractNotFoundException extends GlobalException {

  public PtContractNotFoundException() {
    super(HttpStatus.NOT_FOUND, "존재하지 않는 PT 계약입니다.");
  }
}
