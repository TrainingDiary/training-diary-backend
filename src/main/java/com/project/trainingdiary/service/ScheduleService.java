package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
import com.project.trainingdiary.entity.ScheduleEntity;
import java.util.List;

public interface ScheduleService {

  void createSchedule(OpenScheduleRequestDto dto);

  List<ScheduleEntity> getScheduleList();
}
