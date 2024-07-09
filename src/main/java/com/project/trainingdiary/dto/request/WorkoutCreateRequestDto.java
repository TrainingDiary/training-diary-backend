package com.project.trainingdiary.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutCreateRequestDto {

  private Long workoutTypeId;

  private int weight;
  private int rep;
  private int sets;
  private int time;
  private int speed;

}
