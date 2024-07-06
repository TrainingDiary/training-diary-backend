package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class PtContractAlreadyExistException extends GlobalException {

  public PtContractAlreadyExistException() {
    super(HttpStatus.BAD_REQUEST, "트레이너와 트레이니가 이미 계약이 있습니다.");
  }
}
