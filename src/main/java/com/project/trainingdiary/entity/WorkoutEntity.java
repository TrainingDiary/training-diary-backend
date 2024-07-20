package com.project.trainingdiary.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import com.project.trainingdiary.dto.request.WorkoutCreateRequestDto;
import com.project.trainingdiary.dto.request.WorkoutUpdateRequestDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
@Entity(name = "workout")
public class WorkoutEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  private String workoutTypeName;
  private String targetMuscle;
  private String remarks;

  private Integer weight;
  private Integer rep;
  private Integer sets;
  private Integer time;
  private Integer speed;

  public static WorkoutEntity toEntity(
      WorkoutCreateRequestDto dto,
      WorkoutTypeEntity workoutType
  ) {

    return WorkoutEntity.builder()
        .workoutTypeName(workoutType.getName())
        .targetMuscle(workoutType.getTargetMuscle())
        .remarks(workoutType.getRemarks())
        .weight(dto.getWeight())
        .rep(dto.getRep())
        .sets(dto.getSets())
        .time(dto.getTime())
        .speed(dto.getSpeed())
        .build();

  }

  public static WorkoutEntity updateEntity(
      WorkoutUpdateRequestDto dto,
      WorkoutTypeEntity workoutType, WorkoutEntity workout
  ) {

    return workout.toBuilder()
        .workoutTypeName(workoutType.getName())
        .targetMuscle(workoutType.getTargetMuscle())
        .remarks(workoutType.getRemarks())
        .weight(dto.getWeight())
        .rep(dto.getRep())
        .sets(dto.getSets())
        .time(dto.getTime())
        .speed(dto.getSpeed())
        .build();

  }

}
