package com.project.trainingdiary.entity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import com.project.trainingdiary.model.type.NotificationType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"trainer", "trainee"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "notification")
public class NotificationEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Enumerated(STRING)
  private NotificationType notificationType;

  // NotificationType 별로 넣어놓을 노트를 지정. 예약 관련이면 날짜와 시간을 넣는 식
  private String note;

  private LocalDate eventDate;

  private boolean toTrainer;

  private boolean toTrainee;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "trainer_id")
  private TrainerEntity trainer;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "trainee_id")
  private TraineeEntity trainee;

  public static NotificationEntity of(
      NotificationType notificationType,
      boolean toTrainer,
      boolean toTrainee,
      TrainerEntity trainer,
      TraineeEntity trainee,
      String note,
      LocalDate eventDate
  ) {
    return NotificationEntity.builder()
        .notificationType(notificationType)
        .toTrainee(toTrainee)
        .toTrainer(toTrainer)
        .trainer(trainer)
        .trainee(trainee)
        .note(note)
        .eventDate(eventDate)
        .build();
  }
}
