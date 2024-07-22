package com.project.trainingdiary.dto.request.workout.type;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class WorkoutTypeUpdateRequestDto {

  @Positive
  @NotNull(message = "운동 종류의 id 값을 입력해주세요.")
  @Schema(description = "운동 종류의 id", example = "1")
  private Long workoutTypeId;

  @NotBlank(message = "운동 종류 이름을 입력해주세요.")
  @Schema(description = "운동 종류의 이름", example = "랫 풀 다운")
  private String name;

  @NotBlank(message = "운동 종류의 타겟 부위를 입력해주세요.")
  @Schema(description = "운동 종류의 타겟 부위", example = "등")
  private String targetMuscle;

  @NotBlank(message = "운동 종류에 대한 설명을 입력해주세요.")
  @Schema(description = "운동 종류에 관한 설명", example = "수직 당기기 머신 운동")
  private String remarks;

}

