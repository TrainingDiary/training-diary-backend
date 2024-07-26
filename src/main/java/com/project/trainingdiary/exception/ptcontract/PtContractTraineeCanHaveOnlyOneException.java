package com.project.trainingdiary.exception.ptcontract;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class PtContractTraineeCanHaveOnlyOneException extends GlobalException {

  public PtContractTraineeCanHaveOnlyOneException() {
    super(HttpStatus.GONE, "트레이니가 이미 계약이 있습니다.");
  }
}
