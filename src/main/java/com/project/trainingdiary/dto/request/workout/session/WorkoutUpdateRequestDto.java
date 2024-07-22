package com.project.trainingdiary.dto.request.workout.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
public class WorkoutUpdateRequestDto {

  @Positive
  @NotNull(message = "운동 기록의 id 값을 입력해주세요.")
  @Schema(description = "운동 기록의 id", example = "1")
  private Long workoutId;

  @Positive
  @NotNull(message = "운동 종류의 id 값을 입력해주세요.")
  @Schema(description = "운동 종류의 id", example = "1")
  private Long workoutTypeId;

  @PositiveOrZero
  @Schema(description = "무게", example = "1")
  private Integer weight;

  @PositiveOrZero
  @Schema(description = "횟수", example = "1")
  private Integer rep;

  @PositiveOrZero
  @Schema(description = "세트 수", example = "1")
  private Integer sets;

  @PositiveOrZero
  @Schema(description = "시간", example = "1")
  private Integer time;

  @PositiveOrZero
  @Schema(description = "속도", example = "1")
  private Integer speed;

}
