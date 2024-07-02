package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;

public interface ScheduleService {

  void createSchedule(OpenScheduleRequestDto dto);
}
