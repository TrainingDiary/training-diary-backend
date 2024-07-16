package com.project.trainingdiary.dto.request;

import com.project.trainingdiary.model.GenderType;
import com.project.trainingdiary.model.TargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditTraineeInfoRequestDto {

  @NotNull(message = "traineeId 값은 null이 될 수 없습니다.")
  private Long traineeId;

  @NotNull(message = "birthDate 값은 null이 될 수 없습니다.")
  private LocalDate birthDate;

  @NotNull(message = "gender 값은 null이 될 수 없습니다.")
  private GenderType gender;

  @Positive(message = "height 값은 양수이어야 합니다.")
  private double height;

  @Positive(message = "remainingSessions 값은 양수이어야 합니다.")
  private int remainingSessions;

  @NotNull(message = "targetType 값은 null이 될 수 없습니다.")
  private TargetType targetType;

  @Positive(message = "targetValue 값은 양수이어야 합니다.")
  private double targetValue;

  @NotNull(message = "targetReward 값은 null이 될 수 없습니다.")
  @Size(min = 1, max = 255, message = "targetReward 값은 비어있을 수 없으며 255자를 초과할 수 없습니다.")
  private String targetReward;
}