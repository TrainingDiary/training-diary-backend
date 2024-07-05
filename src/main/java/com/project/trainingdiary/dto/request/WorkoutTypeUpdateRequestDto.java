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

  @NotBlank(message = "운동 종류 이름은 필수 입력사항입니다.")
  private String name;

  @NotBlank(message = "운동 종류의 타겟 부위는 필수 입력사항입니다.")
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

