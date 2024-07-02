package com.project.trainingdiary.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
@Entity(name = "WorkoutType")
public class WorkoutTypeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String targetMuscle;
  private String remarks;

  private boolean weightInputRequired;
  private boolean repInputRequired;
  private boolean setInputRequired;
  private boolean timeInputRequired;
  private boolean speedInputRequired;

  @ManyToOne
  private TrainerEntity trainer;

}
