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
  SIGN_UP_SUCCESS(HttpStatus.OK, "회원가입 성공"),
  SCHEDULE_OPEN_SUCCESS(HttpStatus.OK, "일정 열기 성공");

  private final HttpStatus status;
  private final String message;
}
