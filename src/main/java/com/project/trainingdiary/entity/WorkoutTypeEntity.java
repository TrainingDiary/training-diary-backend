package com.project.trainingdiary.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder(toBuilder = true)
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "workout_type")
public class WorkoutTypeEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
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
  @JoinColumn(name = "trainer_id", referencedColumnName = "trainer_id")
  private TrainerEntity trainer;

}
