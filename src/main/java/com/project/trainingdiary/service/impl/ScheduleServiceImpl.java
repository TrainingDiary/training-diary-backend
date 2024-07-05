package com.project.trainingdiary.service.impl;

import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.exception.impl.ScheduleAlreadyExistException;
import com.project.trainingdiary.exception.impl.ScheduleInvalidException;
import com.project.trainingdiary.repository.ScheduleRepository;
import com.project.trainingdiary.service.ScheduleService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ScheduleServiceImpl implements ScheduleService {

  private final ScheduleRepository scheduleRepository;

  @Override
  @Transactional
  public void createSchedule(OpenScheduleRequestDto dto) {
    //TODO: trainer 확인해서 scheduleEntity에 연결하기

    List<ScheduleEntity> scheduleEntities = dto.toEntities();

    Set<LocalDateTime> existings = scheduleRepository.findByDates(
        getEarliest(scheduleEntities),
        getLatest(scheduleEntities)
    );

    for (ScheduleEntity schedule : scheduleEntities) {
      if (existings.contains(schedule.getStartAt())) {
        throw new ScheduleAlreadyExistException();
      }
    }

    scheduleRepository.saveAll(scheduleEntities);
  }

  public List<ScheduleEntity> getScheduleList() {
    //TODO: 기간으로 받아서 목록 조회하기. 현재는 테스트 확인용
    return scheduleRepository.findAll();
  }

  private static LocalDateTime getLatest(List<ScheduleEntity> scheduleEntities) {
    return scheduleEntities.stream()
        .max(Comparator.comparing(ScheduleEntity::getStartAt))
        .map(ScheduleEntity::getStartAt)
        .orElseThrow(ScheduleInvalidException::new);
  }

  private static LocalDateTime getEarliest(List<ScheduleEntity> scheduleEntities) {
    return scheduleEntities.stream()
        .min(Comparator.comparing(ScheduleEntity::getStartAt))
        .map(ScheduleEntity::getStartAt)
        .orElseThrow(ScheduleInvalidException::new);
  }
}
