package com.project.trainingdiary.dto.response.schedule;

import com.project.trainingdiary.model.type.ScheduleStatusType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CancelScheduleByTrainerResponseDto {

  private long scheduleId;
  private ScheduleStatusType scheduleStatusType;
}
