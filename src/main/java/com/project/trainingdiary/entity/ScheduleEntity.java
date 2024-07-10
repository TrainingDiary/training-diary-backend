package com.project.trainingdiary.entity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

import com.project.trainingdiary.model.ScheduleStatus;
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

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
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
  private ScheduleStatus scheduleStatus;

  @ManyToOne
  @JoinColumn(name = "pt_contract_id")
  private PtContractEntity ptContract;

  @ManyToOne
  @JoinColumn(name = "trainer_id")
  private TrainerEntity trainer;

  public void apply(PtContractEntity ptContract) {
    this.scheduleStatus = ScheduleStatus.RESERVE_APPLIED;
    this.ptContract = ptContract;
  }
}
