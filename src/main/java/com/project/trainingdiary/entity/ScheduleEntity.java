package com.project.trainingdiary.entity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

import com.project.trainingdiary.model.type.ScheduleStatusType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
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

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "schedule")
public class ScheduleEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(nullable = false)
  private LocalDateTime startAt;

  @Column(nullable = false)
  private LocalDateTime endAt;

  @Enumerated(value = STRING)
  @Column(nullable = false)
  private ScheduleStatusType scheduleStatusType;

  @ManyToOne
  @JoinColumn(name = "pt_contract_id")
  private PtContractEntity ptContract;

  @ManyToOne
  @JoinColumn(name = "trainer_id")
  private TrainerEntity trainer;

  public void apply(PtContractEntity ptContract) {
    this.scheduleStatusType = ScheduleStatusType.RESERVE_APPLIED;
    this.ptContract = ptContract;
  }

  public void acceptReserveApplied() {
    this.scheduleStatusType = ScheduleStatusType.RESERVED;
  }

  public void rejectReserveApplied() {
    this.scheduleStatusType = ScheduleStatusType.OPEN;
    this.ptContract = null;
  }

  public void cancel() {
    this.scheduleStatusType = ScheduleStatusType.OPEN;
    this.ptContract = null;
  }

  public static ScheduleEntity of(
      LocalDateTime startAt,
      LocalDateTime endAt,
      TrainerEntity trainer
  ) {
    return ScheduleEntity.builder()
        .startAt(startAt)
        .endAt(endAt)
        .trainer(trainer)
        .scheduleStatusType(ScheduleStatusType.OPEN)
        .build();
  }
}
