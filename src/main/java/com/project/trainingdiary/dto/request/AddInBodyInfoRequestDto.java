package com.project.trainingdiary.dto.request;

import com.project.trainingdiary.entity.InBodyRecordHistoryEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddInBodyInfoRequestDto {

  @NotNull(message = "traineeId 값은 null이 안됩니다.")
  @Positive(message = "traineeId는 양수여야 합니다.")
  private Long traineeId;

  @PositiveOrZero(message = "weight는 0 이상이어야 합니다.")
  private double weight;

  @PositiveOrZero(message = "bodyFatPercentage는 0 이상이어야 합니다.")
  private double bodyFatPercentage;

  @PositiveOrZero(message = "skeletalMuscleMass는 0 이상이어야 합니다.")
  private double skeletalMuscleMass;

  @NotNull(message = "addedDate 값은 null이 안됩니다.")
  private LocalDate addedDate;

  public static InBodyRecordHistoryEntity toEntity(AddInBodyInfoRequestDto dto,
      TraineeEntity trainee) {
    return InBodyRecordHistoryEntity
        .builder()
        .trainee(trainee)
        .weight(dto.getWeight())
        .addedDate(dto.getAddedDate())
        .bodyFatPercentage(dto.getBodyFatPercentage())
        .skeletalMuscleMass(dto.getSkeletalMuscleMass())
        .build();
  }
}