package com.project.trainingdiary.exception.impl;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class MediaCountExceededException extends GlobalException {

  public MediaCountExceededException() {
    super(HttpStatus.PAYLOAD_TOO_LARGE, "미디어 업로드 개수는 각 10개를 넘길 수 없습니다.");
  }
}
