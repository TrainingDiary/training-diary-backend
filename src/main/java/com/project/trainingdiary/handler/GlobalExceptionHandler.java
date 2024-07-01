package com.project.trainingdiary.handler;

import com.project.trainingdiary.dto.response.CommonResponse;
import com.project.trainingdiary.exception.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
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

  @ExceptionHandler(Exception.class)
  public CommonResponse<?> handler(Exception e, HttpServletRequest request) {

    log.error(
        "Exception, {}, {}, {}",
        HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), request.getRequestURI()
    );

    return new CommonResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 에러");
  }
}