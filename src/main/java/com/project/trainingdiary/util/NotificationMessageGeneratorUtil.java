package com.project.trainingdiary.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationMessageGeneratorUtil {

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일 HH:mm");

  /**
   * 트레이니의 일정 신청
   */
  public static String reserveApplied(String traineeName, LocalDateTime startAt) {
    return String.format(
        "%s님이 %s 일정 예약을 신청했습니다.",
        traineeName,
        startAt.format(formatter)
    );
  }

  /**
   * 트레이니의 일정 취소
   */
  public static String reserveCancelByTrainee(String traineeName, LocalDateTime startAt) {
    return String.format(
        "%s님이 %s 일정을 취소했습니다.",
        traineeName,
        startAt.format(formatter)
    );
  }

  /**
   * 트레이너의 일정 수락
   */
  public static String reserveAccept(String trainerName, LocalDateTime startAt) {
    return String.format(
        "%s님이 %s 일정을 수락했습니다.",
        trainerName,
        startAt.format(formatter)
    );
  }

  /**
   * 트레이너의 일정 거절
   */
  public static String reserveReject(String trainerName, LocalDateTime startAt) {
    return String.format(
        "%s님이 %s 일정을 거절했습니다.",
        trainerName,
        startAt.format(formatter)
    );
  }

  /**
   * 트레이너의 일정 취소
   */
  public static String reserveCancelByTrainer(String trainerName, LocalDateTime startAt) {
    return String.format(
        "%s님이 %s 일정을 취소했습니다.",
        trainerName,
        startAt.format(formatter)
    );
  }

  /**
   * 트레이너의 일정 등록
   */
  public static String reserveRegister(String trainerName, int count) {
    return String.format(
        "%s님이 %d개의 일정을 등록했습니다.",
        trainerName,
        count
    );
  }

  /**
   * 트레이너의 트레이니 등록
   */
  public static String createPtContract(String trainerName, String traineeName) {
    return String.format(
        "%s님이 %s을 트레이니로 등록했습니다.",
        trainerName,
        traineeName
    );
  }
}
