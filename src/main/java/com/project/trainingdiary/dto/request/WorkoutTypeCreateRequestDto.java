package com.project.trainingdiary.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
public class WorkoutTypeCreateRequestDto {

  @NotNull(message = "운동 종류의 이름을 입력해주세요.")
  @Schema(description = "운동 종류의 이름", example = "랫 풀 다운")
  private String name;

  @NotNull(message = "운동 종류의 타겟 부위를 입력해주세요.")
  @Schema(description = "운동 종류의 타겟 부위", example = "등")
  private String targetMuscle;

  @NotNull(message = "운동 종류에 대한 설명을 입력해주세요.")
  @Schema(description = "운동 종류에 관한 설명", example = "수직 당기기 머신 운동")
  private String remarks;

  @Schema(description = "운동의 무게 입력이 필요한지 여부",
      example = "true", requiredMode = REQUIRED)
  private boolean weightInputRequired;

  @Schema(description = "운동의 횟수 입력이 필요한지 여부",
      example = "true", requiredMode = REQUIRED)
  private boolean repInputRequired;

  @Schema(description = "운동의 세트 수 입력이 필요한지 여부",
      example = "true", requiredMode = REQUIRED)
  private boolean setInputRequired;

  @Schema(description = "운동의 시간 입력이 필요한지 여부",
      example = "false", requiredMode = REQUIRED)
  private boolean timeInputRequired;

  @Schema(description = "운동의 속도 입력이 필요한지 여부",
      example = "false", requiredMode = REQUIRED)
  private boolean speedInputRequired;

}

