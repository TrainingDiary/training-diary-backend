package com.project.trainingdiary.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationMessageGeneratorUtil {

  public static String reserveApplied(String traineeName, LocalDateTime startAt) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일 HH:mm");

    return String.format(
        "%s님이 %s에 일정 예약을 신청했습니다.",
        traineeName,
        startAt.format(formatter)
    );
  }
}
