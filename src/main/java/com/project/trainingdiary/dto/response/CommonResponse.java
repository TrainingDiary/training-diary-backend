package com.project.trainingdiary.dto.response;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Response 공통화
 *
 * @deprecated 이 클래스 대신 CustomResponse를 사용하세요.
 */
@Deprecated
public class CommonResponse<T> extends ResponseEntity<Map<String, Object>> {

  private static final String STATUS_CODE = "statusCode";
  private static final String ERROR_CODE = "errorCode";
  private static final String MESSAGE = "message";
  private static final String DATA = "data";
  private static final int MIN_ERROR_CODE = 400;

  public CommonResponse(T body, HttpStatus status, String message) {
    super(createResponseMap(body, status, message), status);
  }

  public CommonResponse(HttpStatus status, String message) {
    super(createResponseMap(null, status, message), status);
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

  public static CommonResponse<?> success() {
    return new CommonResponse<>(HttpStatus.OK, "성공");
  }

  public static CommonResponse<?> success(Object body) {
    return new CommonResponse<>(body, HttpStatus.OK, "성공"); // 200
  }

  public static CommonResponse<?> created() {
    return new CommonResponse<>(HttpStatus.CREATED, "생성");
  }

  public static CommonResponse<?> created(Object body) {
    return new CommonResponse<>(body, HttpStatus.CREATED, "생성"); // 201
  }

  public static CommonResponse<?> clientFail() {
    return new CommonResponse<>(HttpStatus.BAD_REQUEST, "잘못된 요청");
  }

  public static CommonResponse<?> clientFail(String message) {
    return new CommonResponse<>(HttpStatus.BAD_REQUEST, message);
  }

  public static CommonResponse<?> serverFail() {
    return new CommonResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러");
  }

  public static CommonResponse<?> serverFail(String message) {
    return new CommonResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, message);
  }
}
