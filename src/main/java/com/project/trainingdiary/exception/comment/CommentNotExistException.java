package com.project.trainingdiary.exception.comment;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class CommentNotExistException extends GlobalException {

  public CommentNotExistException() {
    super(HttpStatus.NOT_FOUND, "식단 댓글이 없습니다.");
  }
}
