package com.project.trainingdiary.dto.request;

import com.project.trainingdiary.entity.TrainerEntity;
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
public class WorkoutTypeCreateRequestDto {

  private Long trainerId;

  @NotBlank(message = "운동 종류 이름은 필수 입력사항입니다.")
  private String name;

  @NotBlank(message = "운동 종류의 타겟 부위는 필수 입력사항입니다.")
  private String targetMuscle;

  private String remarks;

  private boolean weightInputRequired;
  private boolean repInputRequired;
  private boolean setInputRequired;
  private boolean timeInputRequired;
  private boolean speedInputRequired;

  public static WorkoutTypeEntity toEntity(WorkoutTypeCreateRequestDto dto, TrainerEntity entity) {

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

}

