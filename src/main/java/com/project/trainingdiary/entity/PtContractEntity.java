package com.project.trainingdiary.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import com.project.trainingdiary.dto.response.PtContractResponseDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString(exclude = {"trainer", "trainee"})
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "pt_contract")
public class PtContractEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  private int totalSession;

  private int usedSession;

  private LocalDateTime totalSessionUpdatedAt;

  private boolean isTerminated;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "trainer_id")
  private TrainerEntity trainer;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "trainee_id")
  private TraineeEntity trainee;

  public int getRemainSession() {
    return this.totalSession - this.usedSession;
  }

  public void addSession(int addition) {
    this.totalSession += addition;
    this.totalSessionUpdatedAt = LocalDateTime.now();
  }

  public void useSession() {
    this.usedSession += 1;
  }

  public void unuseSession() {
    this.usedSession -= 1;
  }

  public void terminate() {
    isTerminated = true;
  }

  public static PtContractEntity of(TrainerEntity trainer, TraineeEntity trainee,
      int sessionCount) {
    return PtContractEntity.builder()
        .totalSession(sessionCount)
        .totalSessionUpdatedAt(LocalDateTime.now())
        .trainer(trainer)
        .trainee(trainee)
        .build();
  }

  public PtContractResponseDto toResponseDto() {
    return PtContractResponseDto.builder()
        .id(id)
        .trainerId(trainer.getId())
        .traineeId(trainee.getId())
        .usedSession(usedSession)
        .totalSession(totalSession)
        .createdAt(getCreatedAt())
        .totalSessionUpdatedAt(totalSessionUpdatedAt)
        .build();
  }
}
