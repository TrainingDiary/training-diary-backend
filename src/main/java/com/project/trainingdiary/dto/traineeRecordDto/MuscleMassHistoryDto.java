package com.project.trainingdiary.dto.traineeRecordDto;

import com.project.trainingdiary.entity.InBodyRecordHistoryEntity;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MuscleMassHistoryDto {

  private LocalDateTime date;
  private double muscleMass;

  public static MuscleMassHistoryDto fromEntity(InBodyRecordHistoryEntity entity) {
    return MuscleMassHistoryDto.builder()
        .date(entity.getCreatedAt())
        .muscleMass(entity.getSkeletalMuscleMass())
        .build();
  }
}