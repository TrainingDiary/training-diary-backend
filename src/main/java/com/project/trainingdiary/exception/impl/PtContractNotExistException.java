package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class PtContractNotExistException extends GlobalException {

  public PtContractNotExistException() {
    super(HttpStatus.valueOf(460), "계약이 없습니다.");
  }
}
