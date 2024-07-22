package com.project.trainingdiary.dto.request.trainer;

import com.project.trainingdiary.entity.InBodyRecordHistoryEntity;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BodyFatHistoryDto {

  private LocalDate addedDate;
  private double bodyFatPercentage;

  public static BodyFatHistoryDto fromEntity(InBodyRecordHistoryEntity entity) {
    return BodyFatHistoryDto.builder()
        .addedDate(entity.getAddedDate())
        .bodyFatPercentage(entity.getBodyFatPercentage())
        .build();
  }
}