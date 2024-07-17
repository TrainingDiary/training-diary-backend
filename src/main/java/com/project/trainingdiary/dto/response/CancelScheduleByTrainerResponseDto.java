package com.project.trainingdiary.dto.response;

import com.project.trainingdiary.model.ScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CancelScheduleByTrainerResponseDto {

  private long scheduleId;
  private ScheduleStatus scheduleStatus;
}
