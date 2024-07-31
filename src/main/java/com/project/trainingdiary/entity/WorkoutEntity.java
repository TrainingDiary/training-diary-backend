package com.project.trainingdiary.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import com.project.trainingdiary.dto.request.workout.session.WorkoutCreateRequestDto;
import com.project.trainingdiary.dto.request.workout.session.WorkoutUpdateRequestDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
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
      String workoutTypeName,
      String targetMuscle,
      String remarks
  ) {

    return WorkoutEntity.builder()
        .workoutTypeName(workoutTypeName)
        .targetMuscle(targetMuscle)
        .remarks(remarks)
        .weight(dto.getWeight())
        .rep(dto.getRep())
        .sets(dto.getSets())
        .time(dto.getTime())
        .speed(dto.getSpeed())
        .build();

  }

  public static WorkoutEntity updateEntity(
      WorkoutUpdateRequestDto dto,
      String workoutTypeName,
      String targetMuscle,
      String remarks,
      WorkoutEntity workout
  ) {

    return workout.toBuilder()
        .workoutTypeName(workoutTypeName)
        .targetMuscle(targetMuscle)
        .remarks(remarks)
        .weight(dto.getWeight())
        .rep(dto.getRep())
        .sets(dto.getSets())
        .time(dto.getTime())
        .speed(dto.getSpeed())
        .build();

  }

}
