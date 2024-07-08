package com.project.trainingdiary.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ScheduleResponseDetail {

  private Long id;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
  private LocalTime startTime;
  private String trainerName;
  private String traineeName;
  private ScheduleStatus status;
}
