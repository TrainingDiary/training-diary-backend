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

  private Integer weight;
  private Integer rep;
  private Integer sets;
  private Integer time;
  private Integer speed;

}
