package com.project.trainingdiary.handler;

import com.project.trainingdiary.exception.ErrorResponse;
import com.project.trainingdiary.exception.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(GlobalException.class)
  public ResponseEntity<ErrorResponse> handler(GlobalException e, HttpServletRequest request) {

    log.error(
        "GlobalException, {}, {}, {}",
        e.getHttpStatus(), e.getMessage(), request.getRequestURI()
    );

    ErrorResponse response = new ErrorResponse(e.getHttpStatus().value(), e.getMessage());
    return new ResponseEntity<>(response, e.getHttpStatus());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e,
      HttpServletRequest request) {

    log.error(
        "AccessDeniedException, {}, {}, {}",
        HttpStatus.FORBIDDEN, e.getMessage(), request.getRequestURI()
    );

    ErrorResponse response = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "접근 권한이 없습니다.");
    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
  }
}