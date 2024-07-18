package com.project.trainingdiary.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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

  @NotNull
  @Schema(example = "2024-07-17")
  private LocalDate startDate;

  @NotNull
  @Schema(type = "array", example = "[\"10:00\", \"14:00\"]")
  private List<LocalTime> startTimes;
}