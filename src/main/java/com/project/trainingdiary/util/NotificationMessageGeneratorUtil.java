package com.project.trainingdiary.util;

import com.project.trainingdiary.model.NotificationMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationMessageGeneratorUtil {

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일 HH:mm");

  /**
   * 트레이니의 일정 신청
   */
  public static NotificationMessage reserveApplied(String traineeName, LocalDateTime startAt) {
    String title = "일정 신청";
    String body = String.format(
        "%s님이 %s 일정 예약을 신청했습니다.",
        traineeName,
        startAt.format(formatter)
    );
    return new NotificationMessage(title, body);
  }

  /**
   * 트레이니의 일정 취소
   */
  public static NotificationMessage reserveCancelByTrainee(String traineeName,
      LocalDateTime startAt) {
    String title = "일정 취소";
    String body = String.format(
        "%s님이 %s 일정을 취소했습니다.",
        traineeName,
        startAt.format(formatter)
    );
    return new NotificationMessage(title, body);
  }

  /**
   * 트레이너의 일정 수락
   */
  public static NotificationMessage reserveAccept(String trainerName, LocalDateTime startAt) {
    String title = "일정 수락";
    String body = String.format(
        "%s님이 %s 일정을 수락했습니다.",
        trainerName,
        startAt.format(formatter)
    );
    return new NotificationMessage(title, body);
  }

  /**
   * 트레이너의 일정 거절
   */
  public static NotificationMessage reserveReject(String trainerName, LocalDateTime startAt) {
    String title = "일정 거절";
    String body = String.format(
        "%s님이 %s 일정을 거절했습니다.",
        trainerName,
        startAt.format(formatter)
    );
    return new NotificationMessage(title, body);
  }

  /**
   * 트레이너의 일정 취소
   */
  public static NotificationMessage reserveCancelByTrainer(String trainerName,
      LocalDateTime startAt) {
    String title = "일정 취소";
    String body = String.format(
        "%s님이 %s 일정을 취소했습니다.",
        trainerName,
        startAt.format(formatter)
    );
    return new NotificationMessage(title, body);
  }

  /**
   * 트레이너의 일정 등록
   */
  public static NotificationMessage reserveRegister(String trainerName, int count) {
    String title = "일정 등록";
    String body = String.format(
        "%s님이 %d개의 일정을 등록했습니다.",
        trainerName,
        count
    );
    return new NotificationMessage(title, body);
  }

  /**
   * 트레이너의 트레이니 등록
   */
  public static NotificationMessage createPtContract(String trainerName, String traineeName) {
    String title = "트레이니 등록";
    String body = String.format(
        "%s님이 %s을 트레이니로 등록했습니다.",
        trainerName,
        traineeName
    );
    return new NotificationMessage(title, body);
  }

  /**
   * PT 1시간 전 알림
   */
  public static NotificationMessage oneHourBeforePtSession(String trainerName, String traineeName,
      LocalDateTime startAt) {
    String title = "PT 1시간 전 알림";
    String body = String.format(
        "%s님과 %s님의 %s PT가 1시간 후에 시작됩니다.",
        trainerName,
        traineeName,
        startAt.format(formatter)
    );
    return new NotificationMessage(title, body);
  }
}
