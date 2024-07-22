package com.project.trainingdiary.dto.request.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptScheduleRequestDto {

  @NotNull(message = "일정 id를 입력해주세요")
  @Schema(example = "1")
  private Long scheduleId;
}
