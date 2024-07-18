package com.project.trainingdiary.dto.traineeRecordDto;

import com.project.trainingdiary.entity.InBodyRecordHistoryEntity;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeightHistoryDto {

  private LocalDate addedDate;
  private double weight;

  public static WeightHistoryDto fromEntity(InBodyRecordHistoryEntity entity) {
    return WeightHistoryDto.builder()
        .addedDate(entity.getAddedDate())
        .weight(entity.getWeight())
        .build();
  }
}