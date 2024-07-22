package com.project.trainingdiary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
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
public class WorkoutSessionUpdateRequestDto {

  @Positive @NotNull(message = "운동 일지의 id 값을 입력해주세요.")
  @Schema(description = "운동 일지의 id", example = "1")
  private Long sessionId;

  @NotNull(message = "PT 수업 날짜를 입력해주세요.")
  @Schema(description = "PT 수업 날짜", example = "2024-01-01")
  private LocalDate sessionDate;

  @NotNull(message = "PT 수업 중 특이사항을 입력해주세요.")
  @Schema(description = "특이사항", example = "(트레이니에게 전달할 내용)")
  private String specialNote;

  @NotNull(message = "운동 기록을 입력해주세요.")
  @Size(min = 1, message = "최소 한가지 이상의 운동을 포함해야 합니다.")
  @Schema(description = "운동 기록")
  private List<WorkoutUpdateRequestDto> workouts;

}
