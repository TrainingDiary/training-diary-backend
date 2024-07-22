package com.project.trainingdiary.dto.response.workout.session;

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
public class WorkoutResponseDto {

  private Long workoutId;

  private String workoutTypeName;
  private String targetMuscle;
  private String remarks;

  private Integer weight;
  private Integer rep;
  private Integer sets;
  private Integer time;
  private Integer speed;

  public static WorkoutResponseDto fromEntity(WorkoutEntity workout) {

    return WorkoutResponseDto.builder()
        .workoutId(workout.getId())
        .workoutTypeName(workout.getWorkoutTypeName())
        .targetMuscle(workout.getTargetMuscle())
        .remarks(workout.getRemarks())
        .weight(workout.getWeight())
        .rep(workout.getRep())
        .sets(workout.getSets())
        .time(workout.getTime())
        .speed(workout.getSpeed())
        .build();
  }

}
