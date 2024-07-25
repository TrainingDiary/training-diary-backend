package com.project.trainingdiary.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import com.project.trainingdiary.dto.response.ptcontract.PtContractResponseDto;
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
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"trainer", "trainee"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

  public int getRemainingSession() {
    return this.totalSession - this.usedSession;
  }

  public void addSession(int addition) {
    this.totalSession += addition;
    this.totalSessionUpdatedAt = LocalDateTime.now();
  }

  public void useSession() {
    this.usedSession += 1;
  }

  public void restoreSession() {
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
        .ptContractId(id)
        .trainerId(trainer.getId())
        .trainerName(trainer.getName())
        .traineeId(trainee.getId())
        .traineeName(trainee.getName())
        .remainingSession(getRemainingSession())
        .totalSessionUpdatedAt(totalSessionUpdatedAt.toLocalDate())
        .build();
  }
}
