package com.project.trainingdiary.dto.request;

import com.project.trainingdiary.entity.WorkoutEntity;
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
public class WorkoutDto {

  private Long workoutTypeId;

  private String workoutTypeName;
  private String targetMuscle;
  private String remarks;

  private int weight;
  private int rep;
  private int sets;
  private int time;
  private int speed;

  public static WorkoutDto fromEntity(WorkoutEntity entity) {

    return WorkoutDto.builder()
        .workoutTypeName(entity.getWorkoutTypeName())
        .targetMuscle(entity.getTargetMuscle())
        .remarks(entity.getRemarks())
        .weight(entity.getWeight())
        .rep(entity.getRep())
        .sets(entity.getSets())
        .time(entity.getTime())
        .speed(entity.getSpeed())
        .build();

  }

}
