package com.project.trainingdiary.handler;

import com.project.trainingdiary.dto.response.CommonResponse;
import com.project.trainingdiary.exception.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(GlobalException.class)
  public CommonResponse<?> handler(GlobalException e, HttpServletRequest request) {

    log.error(
        "GlobalException, {}, {}, {}",
        e.getHttpStatus(), e.getMessage(), request.getRequestURI()
    );

    return new CommonResponse<>(e.getHttpStatus(), e.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public CommonResponse<?> handleAccessDeniedException(AccessDeniedException e,
      HttpServletRequest request) {
    log.error(
        "AccessDeniedException, {}, {}, {}",
        HttpStatus.FORBIDDEN, e.getMessage(), request.getRequestURI()
    );

    return new CommonResponse<>(HttpStatus.FORBIDDEN, "접근이 없습니다.");
  }
}