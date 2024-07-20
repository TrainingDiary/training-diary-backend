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

  public void sendPushNotification(NotificationEntity notification) {
    if (notification.isToTrainee() && notification.getTrainee().getFcmToken() != null) {
      send(
          notification.getTrainee().getFcmToken().getToken(),
          notification.getNote()
      );
    }
    if (notification.isToTrainer() && notification.getTrainer().getFcmToken() != null) {
      send(
          notification.getTrainer().getFcmToken().getToken(),
          notification.getNote()
      );
    }
  }

  private void send(String token, String message) {
    Message msg = Message.builder()
        .setNotification(
            Notification.builder()
                .setBody(message)
                .build()
        )
        .setToken(token)
        .build();

    try {
      String response = FirebaseMessaging.getInstance().send(msg);
      log.debug("메시지 전송 완료: " + response);
    } catch (Exception e) {
      log.error("메시지 전송 실패: " + e.getMessage());
    }
  }
}
