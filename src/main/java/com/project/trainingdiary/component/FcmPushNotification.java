package com.project.trainingdiary.component;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.project.trainingdiary.entity.NotificationEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FcmPushNotification {

  private static final String logoUrl = "https://training-diary-brand.s3.ap-northeast-2.amazonaws.com/training_diary_logo_60x60.png";

  public void sendPushNotification(NotificationEntity notification) {
    if (notification.isToTrainee() && notification.getTrainee().getFcmToken() != null) {
      send(
          notification.getTrainee().getFcmToken().getToken(),
          notification.getTitle(),
          notification.getBody()
      );
    }
    if (notification.isToTrainer() && notification.getTrainer().getFcmToken() != null) {
      send(
          notification.getTrainer().getFcmToken().getToken(),
          notification.getTitle(),
          notification.getBody()
      );
    }
  }

  private void send(String token, String title, String body) {
    Message msg = Message.builder()
        .setNotification(
            Notification.builder()
                .setTitle(title)
                .setBody(body)
                .setImage(logoUrl)
                .build()
        )
        .setToken(token)
        .build();

    try {
      String response = FirebaseMessaging.getInstance().send(msg);
      log.debug("메시지 전송 완료: {}", response);
    } catch (Exception e) {
      // Requested entity was not found. 에러가 나오면 token 이 더 이상 유효하지 않아서 일 수 있음
      log.error("메시지 전송 실패: {}", e.getMessage());
    }
  }
}
