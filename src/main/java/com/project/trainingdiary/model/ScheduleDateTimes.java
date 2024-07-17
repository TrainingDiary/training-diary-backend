package com.project.trainingdiary.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.LocalDate;
import java.time.LocalTime;
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
public class ScheduleDateTimes {

  @Schema(example = "2024-07-17", requiredMode = RequiredMode.REQUIRED)
  private LocalDate startDate;

  @Schema(type = "array", example = "[\"10:00\", \"14:00\"]", requiredMode = RequiredMode.REQUIRED)
  private List<LocalTime> startTimes;
}