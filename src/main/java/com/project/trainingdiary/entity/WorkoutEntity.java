package com.project.trainingdiary.entity;

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
import lombok.ToString;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "workout")
public class WorkoutEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  private String name;

  private int weight;
  private int rep;
  private int sets;
  private int time;
  private int speed;

  @OneToOne
  @JoinColumn(name = "workout_type_id")
  private WorkoutTypeEntity workoutType;

}
