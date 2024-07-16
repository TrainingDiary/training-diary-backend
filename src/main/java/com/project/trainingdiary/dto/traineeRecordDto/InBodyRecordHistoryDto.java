package com.project.trainingdiary.dto.traineeRecordDto;

import com.project.trainingdiary.entity.InBodyRecordHistoryEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InBodyRecordHistoryDto {

  private long id;
  private double weight;
  private double bodyFatPercentage;
  private double skeletalMuscleMass;

  public static InBodyRecordHistoryDto fromEntity(InBodyRecordHistoryEntity entity) {
    return InBodyRecordHistoryDto.builder()
        .id(entity.getId())
        .weight(entity.getWeight())
        .bodyFatPercentage(entity.getBodyFatPercentage())
        .skeletalMuscleMass(entity.getSkeletalMuscleMass())
        .build();
  }
}