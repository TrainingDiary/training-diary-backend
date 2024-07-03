package com.project.trainingdiary.provider;

import com.project.trainingdiary.exception.impl.EmailSendErrorException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailProvider {

  private final JavaMailSender javaMailSender;
  private final String SUBJECT = "[트레이너 다이어리] 인증 매일입니다.";

  public void sendVerificationEmail(String email, String verificationNumber) {
    try {
      MimeMessage message = javaMailSender.createMimeMessage();
      MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");

      String htmlContent = getVerificationMessage(verificationNumber);

      messageHelper.setTo(email);
      messageHelper.setSubject(SUBJECT);
      messageHelper.setText(htmlContent, true);
      javaMailSender.send(message);

    } catch (MessagingException e) {
      log.error("Email 전송 실패 {}", e.getMessage());
      throw new EmailSendErrorException();
    }
  }

  private String getVerificationMessage(String verificationNumber) {

    String verificationMessage = "";
    verificationMessage += "<h1 style='text-align: center;'> [트레이너 다이어리] 인증매일</h1>";
    verificationMessage +=
        "<h3 style='text-align: center;'> 인증코드 : <strong style='font-size: 32px; letter-spacing: 8px;'>"
            + verificationNumber + "</strong></h3>";

    return verificationMessage;
  }
}
