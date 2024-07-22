package com.project.trainingdiary.handler;

import com.project.trainingdiary.exception.ErrorResponse;
import com.project.trainingdiary.exception.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 프로젝트에서 생성한 예외를 처리합니다.
   *
   * @param e Exception
   * @return ResponseEntity<ErrorResponse> - 전역 예외 응답
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleExceptionClass(
      Exception e, HttpServletRequest request) {

    log.error(
        "Exception, {}, {}, {}",
        HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), request.getRequestURI()
    );

    ErrorResponse response = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
        e.getMessage());
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }


  /**
   * 프로젝트에서 생성한 예외를 처리합니다.
   *
   * @param e GlobalException
   * @return ResponseEntity<ErrorResponse> - 전역 예외 응답
   */
  @ExceptionHandler(GlobalException.class)
  public ResponseEntity<ErrorResponse> handler(GlobalException e, HttpServletRequest request) {

    log.error(
        "GlobalException, {}, {}, {}",
        e.getHttpStatus(), e.getMessage(), request.getRequestURI()
    );

    ErrorResponse response = new ErrorResponse(e.getHttpStatus().value(), e.getMessage());
    return new ResponseEntity<>(response, e.getHttpStatus());
  }

  /**
   * 필드 유효성 검사 예외를 처리합니다.
   *
   * @param e MethodArgumentNotValidException
   * @return ResponseEntity<ErrorResponse> - 필드 유효성 검사 오류 응답
   */
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

  /**
   * 유효성 검사 예외를 처리합니다.
   *
   * @param e MethodArgumentNotValidException
   * @return ResponseEntity<ErrorResponse> - 유효성 검사 오류 응답
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException e, HttpServletRequest request) {

    log.error(
        "MethodArgumentNotValidException, {}, {}, {}",
        HttpStatus.BAD_REQUEST, e.getMessage(), request.getRequestURI()
    );

    ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  /**
   * JSON 형식 오류 예외를 처리합니다.
   *
   * @param e HttpMessageNotReadableException
   * @return ResponseEntity<ErrorResponse> - 잘못된 JSON 요청 오류 응답
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleExceptionClass(
      HttpMessageNotReadableException e, HttpServletRequest request) {

    log.error(
        "HttpMessageNotReadableException, {}, {}, {}",
        HttpStatus.BAD_REQUEST, e.getMessage(), request.getRequestURI()
    );

    ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }
}