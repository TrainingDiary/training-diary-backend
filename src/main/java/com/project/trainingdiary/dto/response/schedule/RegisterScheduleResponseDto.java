package com.project.trainingdiary.dto.response.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterScheduleResponseDto {

  private int reservedSession;
  private int remainingSession;
}
