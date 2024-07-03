package com.project.trainingdiary.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "verification")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  private String email;
  private String verificationCode;

  private LocalDateTime expiredAt;

  public static VerificationEntity of(String email, String verificationCode) {
    return VerificationEntity.builder()
        .email(email)
        .verificationCode(verificationCode)
        .expiredAt(LocalDateTime.now().plusMinutes(10))
        .build();
  }
}

