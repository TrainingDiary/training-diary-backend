package com.project.trainingdiary.exception.notification;

import com.project.trainingdiary.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class UnsupportedNotificationTypeException extends GlobalException {

  public UnsupportedNotificationTypeException() {
    super(HttpStatus.INTERNAL_SERVER_ERROR, "지원하지 않는 알림 타입입니다.");
  }
}
