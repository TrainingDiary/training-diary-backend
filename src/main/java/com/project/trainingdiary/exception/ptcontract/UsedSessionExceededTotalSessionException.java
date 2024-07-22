package com.project.trainingdiary.exception.ptcontract;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class UsedSessionExceededTotalSessionException extends GlobalException {

  public UsedSessionExceededTotalSessionException() {
    super(HttpStatus.EXPECTATION_FAILED, "전체 세션 갯수를 다 사용했습니다.");
  }
}
