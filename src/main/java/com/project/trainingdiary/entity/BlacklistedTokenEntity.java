package com.project.trainingdiary.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "blacklisted_token")
public class BlacklistedTokenEntity {

  @Id
  private String token;

  private LocalDateTime expiryDate;
}
