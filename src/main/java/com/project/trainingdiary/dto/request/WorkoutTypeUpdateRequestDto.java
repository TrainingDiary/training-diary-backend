package com.project.trainingdiary.dto.request;

import com.project.trainingdiary.entity.WorkoutTypeEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutTypeUpdateRequestDto {

  @NotBlank
  private String name;

  @NotBlank
  private String targetMuscle;

  private String remarks;

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

