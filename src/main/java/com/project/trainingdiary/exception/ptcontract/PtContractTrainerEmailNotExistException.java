package com.project.trainingdiary.exception.ptcontract;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class PtContractTrainerEmailNotExistException extends GlobalException {

  public PtContractTrainerEmailNotExistException() {
    super(HttpStatus.NOT_ACCEPTABLE, "트레이니 이메일이 존재하지 않아 계약할 수 없습니다.");
  }
}
