package com.project.trainingdiary.dto.traineeRecordDto;

import com.project.trainingdiary.entity.InBodyRecordHistoryEntity;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BodyFatHistoryDto {

  private LocalDateTime date;
  private double bodyFatPercentage;

  public static BodyFatHistoryDto fromEntity(InBodyRecordHistoryEntity entity) {
    return BodyFatHistoryDto.builder()
        .date(entity.getCreatedAt())
        .bodyFatPercentage(entity.getBodyFatPercentage())
        .build();
  }
}