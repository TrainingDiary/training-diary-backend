package com.project.trainingdiary.dto.request.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CloseScheduleRequestDto {

  @NotNull(message = "scheduleIds를 입력해주세요")
  @Schema(type = "array", example = "[1]")
  public List<Long> scheduleIds;
}