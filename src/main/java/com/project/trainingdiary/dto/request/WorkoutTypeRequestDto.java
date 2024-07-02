package com.project.trainingdiary.dto.request;

import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.entity.WorkoutTypeEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutTypeRequestDto {

  private Long trainerId;

  private String name;
  private String targetMuscle;
  private String remarks;

  private boolean weightInputRequired;
  private boolean repInputRequired;
  private boolean setInputRequired;
  private boolean timeInputRequired;
  private boolean speedInputRequired;

  public static WorkoutTypeEntity toEntity(WorkoutTypeRequestDto dto, TrainerEntity entity) {

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
