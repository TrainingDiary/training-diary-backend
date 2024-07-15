package com.project.trainingdiary.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(Include.NON_NULL)
public class ScheduleResponseDetail {

  private Long scheduleId;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
  private LocalTime startTime;
  private Long trainerId;
  private String trainerName;
  private Long traineeId;
  private String traineeName;
  private ScheduleStatus status;
}
