package com.project.trainingdiary.service.impl;

import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.repository.ScheduleRepository;
import com.project.trainingdiary.service.ScheduleService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

  private final ScheduleRepository scheduleRepository;

  @Override
  public void createSchedule(OpenScheduleRequestDto dto) {
    //TODO: trainer 확인해서 scheduleEntity에 연결하기
    List<ScheduleEntity> scheduleEntities = dto.toEntities();
    scheduleRepository.saveAll(scheduleEntities);
  }
}
