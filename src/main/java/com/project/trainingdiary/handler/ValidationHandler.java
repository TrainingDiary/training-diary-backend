package com.project.trainingdiary.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ValidationHandler {

  /**
   * 유효성 검사 예외를 처리합니다.
   *
   * @param ex MethodArgumentNotValidException
   * @return CustomResponse<Map < String, List < String>>> - 유효성 검사 오류 응답
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, List<String>>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    List<String> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(FieldError::getDefaultMessage)
        .toList();

    log.error("Validation exception: ", ex);
    return new ResponseEntity<>(getErrorsMap(errors), new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }

  /**
   * JSON 형식 오류 예외를 처리합니다.
   *
   * @param ex HttpMessageNotReadableException
   * @return CustomResponse<Map < String, List < String>>> - 잘못된 JSON 요청 오류 응답
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, List<String>>> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {
    List<String> errors = List.of("Malformed JSON request");

    log.error("Malformed JSON request: ", ex);
    return new ResponseEntity<>(getErrorsMap(errors), new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }


  /**
   * 오류 목록을 맵 형식으로 변환합니다.
   *
   * @param errors 오류 목록
   * @return Map<String, List < String>> - 오류 맵
   */
  private Map<String, List<String>> getErrorsMap(List<String> errors) {
    Map<String, List<String>> errorResponse = new HashMap<>();
    errorResponse.put("errors", errors);
    return errorResponse;
  }
}
