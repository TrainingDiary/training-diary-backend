package com.project.trainingdiary.service.impl;

import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.exception.impl.ScheduleAlreadyExistException;
import com.project.trainingdiary.exception.impl.ScheduleInvalidException;
import com.project.trainingdiary.repository.ScheduleRepository;
import com.project.trainingdiary.service.ScheduleService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
