package com.project.trainingdiary.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class BlacklistedTokenEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String token;

    private LocalDateTime expiryDate;
}
