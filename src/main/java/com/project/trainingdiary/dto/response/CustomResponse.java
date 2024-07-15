package com.project.trainingdiary.dto.response;

import com.project.trainingdiary.exception.GlobalException;
import com.project.trainingdiary.model.SuccessMessage;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Response 공통화. 성공의 경우 SuccessMessage를 정의 후 사용해주세요. 실패의 경우 GlobalException을 활용해주세요.
 *
 * @deprecated 이 클래스 대신 ResponseEntity를 사용하세요.
 */
@Deprecated
public class CustomResponse<T> extends ResponseEntity<Map<String, Object>> {

  private static final String STATUS_CODE = "statusCode";
  private static final String ERROR_CODE = "errorCode";
  private static final String MESSAGE = "message";
  private static final String DATA = "data";
  private static final int MIN_ERROR_CODE = 400;

  public CustomResponse(T body, HttpStatus status, String message) {
    super(createResponseMap(body, status, message), status);
  }

  public CustomResponse(HttpStatus status, String message) {
    super(createResponseMap(new HashMap<>(), status, message), status);
  }

  private static <T> Map<String, Object> createResponseMap(
      T body, HttpStatus status, String message
  ) {
    HashMap<String, Object> response = new HashMap<>();

    if (status.value() < MIN_ERROR_CODE) {
      response.put(STATUS_CODE, status.value());
      response.put(DATA, body);
    } else {
      response.put(ERROR_CODE, status.value());
    }
    response.put(MESSAGE, message);

    return response;
  }

  public static <T> CustomResponse<T> success() {
    return new CustomResponse<>(HttpStatus.OK, "성공");
  }

  public static <T> CustomResponse<T> success(T body) {
    return new CustomResponse<>(body, HttpStatus.OK, "성공");
  }

  public static <T> CustomResponse<T> success(SuccessMessage message) {
    return new CustomResponse<>(message.getStatus(), message.getMessage());
  }

  public static <T> CustomResponse<T> success(T body, SuccessMessage message) {
    return new CustomResponse<>(body, message.getStatus(), message.getMessage());
  }

  public static CustomResponse<Void> fail(GlobalException exception) {
    return new CustomResponse<>(exception.getHttpStatus(), exception.getMessage());
  }
}
