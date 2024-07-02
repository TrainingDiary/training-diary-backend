package com.project.trainingdiary.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "verification")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerificationEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  private String email;
  private String verificationCode;

  private LocalDateTime expiredAt;

  public static VerificationEntity of(String email, String verificationCode) {
    VerificationEntity entity = new VerificationEntity();
    entity.setEmail(email);
    entity.setVerificationCode(verificationCode);
    entity.setExpiredAt(LocalDateTime.now().plusMinutes(10));
    return entity;
  }
}

