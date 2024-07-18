package com.project.trainingdiary.dto.request;

import com.project.trainingdiary.model.GenderType;
import com.project.trainingdiary.model.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditTraineeInfoRequestDto {

  @Schema(example = "1")
  @NotNull(message = "traineeId 값은 null이 될 수 없습니다.")
  private Long traineeId;

  @Schema(example = "1995-01-01")
  @NotNull(message = "birthDate 값은 null이 될 수 없습니다.")
  private LocalDate birthDate;

  @Schema(example = "MALE", allowableValues = {"MALE", "FEMALE"})
  @NotNull(message = "gender 값은 null이 될 수 없습니다.")
  private GenderType gender;

  @Schema(example = "180.0")
  @Positive(message = "height 값은 양수이어야 합니다.")
  private double height;

  @Schema(example = "5")
  @Positive(message = "remainingSessions 값은 양수이어야 합니다.")
  private int remainingSessions;

  @Schema(example = "TARGET_WEIGHT", allowableValues = {"TARGET_WEIGHT",
      "TARGET_BODY_FAT_PERCENTAGE", "TARGET_SKELETAL_MUSCLE_MASS"})
  @NotNull(message = "targetType 값은 null이 될 수 없습니다.")
  private TargetType targetType;

  @Schema(example = "100.0")
  @Positive(message = "targetValue 값은 양수이어야 합니다.")
  private double targetValue;

  @Schema(example = "맥북프로 사기.")
  @NotNull(message = "targetReward 값은 null이 될 수 없습니다.")
  @Size(min = 1, max = 255, message = "targetReward 값은 비어있을 수 없으며 255자를 초과할 수 없습니다.")
  private String targetReward;
}