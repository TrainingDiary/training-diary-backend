package com.project.trainingdiary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterScheduleResponseDto {

  private int reservedSession;
  private int remainSession;
}
