package com.project.trainingdiary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessMessage {

  NO_DUPLICATE_EMAIL(HttpStatus.OK, "중복이 없습니다."),
  SENT_VERIFICATION_SUCCESS(HttpStatus.OK, "인증 코드 보내기 성공."),
  VERIFICATION_SUCCESS(HttpStatus.OK, "인증 성공."),
  SIGN_UP_SUCCESS(HttpStatus.CREATED, "회원가입 성공"),
  SIGN_IN_SUCCESS(HttpStatus.OK, "로그인 성공"),
  SCHEDULE_OPEN_SUCCESS(HttpStatus.CREATED, "예약 성공"),
  SIGN_OUT_SUCCESS(HttpStatus.OK, "로그아웃 성공");

  private final HttpStatus status;
  private final String message;
}
