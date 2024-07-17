package com.project.trainingdiary.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CancelScheduleByTrainerRequestDto {

  @NotNull(message = "일정 id를 입력해주세요")
  private Long scheduleId;
}
