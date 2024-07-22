package com.project.trainingdiary.exception.trainer;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class TrainerCommentNotExistException extends GlobalException {

  public TrainerCommentNotExistException() {
    super(HttpStatus.NOT_FOUND, "트레이너의 식단 댓글이 없습니다.");
  }
}
