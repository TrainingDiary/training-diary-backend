package com.project.trainingdiary.exception.ptcontract;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class PtContractNotExistException extends GlobalException {

  public PtContractNotExistException() {
    super(HttpStatus.NOT_FOUND, "계약이 없습니다.");
  }
}
