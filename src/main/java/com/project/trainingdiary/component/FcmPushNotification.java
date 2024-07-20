package com.project.trainingdiary.component;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FcmPushNotification {

  private static final String MESSAGE_KEY = "message";

  public void sendPushNotification(String token, String message) {
    Message msg = Message.builder()
        .putData(MESSAGE_KEY, message)
        .setToken(token)
        .build();

    try {
      String response = FirebaseMessaging.getInstance().send(msg);
      log.debug("메시지 전송 완료: " + response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }
}
