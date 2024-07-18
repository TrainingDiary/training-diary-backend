package com.project.trainingdiary.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutUpdateRequestDto {

  private Long workoutId;
  private Long workoutTypeId;

  private int weight;
  private int rep;
  private int sets;
  private int time;
  private int speed;

}
