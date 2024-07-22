package com.project.trainingdiary.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "verification")
public class VerificationEntity extends BaseEntity {

  @Id
  private String email;

  private String verificationCode;

  private LocalDateTime expiredAt;

  private boolean isVerified;

  public static VerificationEntity of(String email, String verificationCode) {
    return VerificationEntity.builder()
        .email(email)
        .verificationCode(verificationCode)
        .expiredAt(LocalDateTime.now().plusMinutes(10))
        .isVerified(false)
        .build();
  }
}

