package com.project.trainingdiary.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
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

@Entity(name = "in_body_record_history")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InBodyRecordHistoryEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private long id;

  private double weight;
  private double bodyFatPercentage;
  private double skeletalMuscleMass;
  private LocalDate addedDate;

  @ManyToOne
  @JoinColumn(name = "trainee_id", nullable = false)
  private TraineeEntity trainee;
}