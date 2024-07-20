package com.project.trainingdiary.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString(exclude = {"trainer", "trainee"})
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "fcm_token")
public class FcmTokenEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  private String token;

  @OneToOne(fetch = LAZY)
  @JoinColumn(name = "trainer_id")
  private TrainerEntity trainer;

  @OneToOne(fetch = LAZY)
  @JoinColumn(name = "trainee_id")
  private TraineeEntity trainee;

  public static FcmTokenEntity of(
      String token,
      TrainerEntity trainer,
      TraineeEntity trainee
  ) {
    return FcmTokenEntity.builder()
        .token(token)
        .trainer(trainer)
        .trainee(trainee)
        .build();
  }
}
