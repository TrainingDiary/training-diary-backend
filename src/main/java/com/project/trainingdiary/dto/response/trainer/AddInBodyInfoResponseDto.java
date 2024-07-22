package com.project.trainingdiary.dto.response.trainer;

import com.project.trainingdiary.entity.InBodyRecordHistoryEntity;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddInBodyInfoResponseDto {

  private Long id;
  private double weight;
  private double bodyFatPercentage;
  private double skeletalMuscleMass;
  private LocalDate addedDate;

  public static AddInBodyInfoResponseDto fromEntity(InBodyRecordHistoryEntity entity) {
    return AddInBodyInfoResponseDto.builder()
        .id(entity.getId())
        .weight(entity.getWeight())
        .bodyFatPercentage(entity.getBodyFatPercentage())
        .skeletalMuscleMass(entity.getSkeletalMuscleMass())
        .addedDate(entity.getAddedDate())
        .build();
  }
}