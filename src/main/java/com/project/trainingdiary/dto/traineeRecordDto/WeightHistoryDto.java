package com.project.trainingdiary.dto.traineeRecordDto;

import com.project.trainingdiary.entity.InBodyRecordHistoryEntity;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeightHistoryDto {

  private LocalDateTime date;
  private double weight;

  public static WeightHistoryDto fromEntity(InBodyRecordHistoryEntity entity) {
    return WeightHistoryDto.builder()
        .date(entity.getCreatedAt())
        .weight(entity.getWeight())
        .build();
  }
}