package com.project.trainingdiary.exception.ptcontract;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class PtContractNotEnoughSessionException extends GlobalException {

  public PtContractNotEnoughSessionException() {
    super(HttpStatus.NOT_ACCEPTABLE, "PT 횟수가 부족합니다.");
  }
}
