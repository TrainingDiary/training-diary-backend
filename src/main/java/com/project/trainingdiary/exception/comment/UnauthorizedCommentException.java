package com.project.trainingdiary.exception.comment;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class UnauthorizedCommentException extends GlobalException {

  public UnauthorizedCommentException() {
    super(HttpStatus.FORBIDDEN, "트레이너의 식단 댓글이 없습니다.");
  }
}
