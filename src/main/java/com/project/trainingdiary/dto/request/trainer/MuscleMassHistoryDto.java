package com.project.trainingdiary.dto.request.trainer;

import com.project.trainingdiary.entity.InBodyRecordHistoryEntity;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MuscleMassHistoryDto {

  private LocalDate addedDate;
  private double muscleMass;

  public static MuscleMassHistoryDto fromEntity(InBodyRecordHistoryEntity entity) {
    return MuscleMassHistoryDto.builder()
        .addedDate(entity.getAddedDate())
        .muscleMass(entity.getSkeletalMuscleMass())
        .build();
  }
}