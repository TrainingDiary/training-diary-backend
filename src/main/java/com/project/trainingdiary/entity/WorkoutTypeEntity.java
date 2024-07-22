package com.project.trainingdiary.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import com.project.trainingdiary.dto.request.workout.type.WorkoutTypeCreateRequestDto;
import com.project.trainingdiary.dto.request.workout.type.WorkoutTypeUpdateRequestDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
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

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "trainer_id", referencedColumnName = "trainer_id")
  private TrainerEntity trainer;

  public static WorkoutTypeEntity toEntity(
      WorkoutTypeCreateRequestDto dto,
      TrainerEntity entity
  ) {

    return WorkoutTypeEntity.builder()
        .name(dto.getName())
        .targetMuscle(dto.getTargetMuscle())
        .remarks(dto.getRemarks())
        .weightInputRequired(dto.isWeightInputRequired())
        .repInputRequired(dto.isRepInputRequired())
        .setInputRequired(dto.isSetInputRequired())
        .timeInputRequired(dto.isTimeInputRequired())
        .speedInputRequired(dto.isSpeedInputRequired())
        .trainer(entity)
        .build();

  }

  public static WorkoutTypeEntity updateEntity(
      WorkoutTypeUpdateRequestDto dto,
      WorkoutTypeEntity entity
  ) {

    return entity.toBuilder()
        .name(dto.getName())
        .targetMuscle(dto.getTargetMuscle())
        .remarks(dto.getRemarks())
        .build();

  }

}
