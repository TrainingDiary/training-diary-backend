package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class UsedSessionExceededTotalSession extends GlobalException {

  public UsedSessionExceededTotalSession() {
    super(HttpStatus.EXPECTATION_FAILED, "전체 세션 갯수를 다 사용했습니다.");
  }
}
