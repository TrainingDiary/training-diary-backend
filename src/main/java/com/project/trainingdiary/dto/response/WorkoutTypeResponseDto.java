package com.project.trainingdiary.dto.response;

import com.project.trainingdiary.entity.WorkoutTypeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkoutTypeResponseDto {

  private Long id;
  private String name;
  private String targetMuscle;
  private String remarks;

  private boolean weightInputRequired;
  private boolean repInputRequired;
  private boolean setInputRequired;
  private boolean timeInputRequired;
  private boolean speedInputRequired;

  public static WorkoutTypeResponseDto fromEntity(WorkoutTypeEntity entity) {

    return WorkoutTypeResponseDto.builder()
        .id(entity.getId())
        .name(entity.getName())
        .targetMuscle(entity.getTargetMuscle())
        .remarks(entity.getRemarks())
        .weightInputRequired(entity.isWeightInputRequired())
        .repInputRequired(entity.isRepInputRequired())
        .setInputRequired(entity.isSetInputRequired())
        .timeInputRequired(entity.isTimeInputRequired())
        .speedInputRequired(entity.isSpeedInputRequired())
        .build();

  }

}
