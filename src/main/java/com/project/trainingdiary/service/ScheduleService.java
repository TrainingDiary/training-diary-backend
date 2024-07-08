package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
import com.project.trainingdiary.dto.response.ScheduleResponseDto;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.exception.impl.ScheduleAlreadyExistException;
import com.project.trainingdiary.exception.impl.ScheduleInvalidException;
import com.project.trainingdiary.exception.impl.ScheduleRangeTooLong;
import com.project.trainingdiary.repository.ScheduleRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

  private static final int MAX_QUERY_DAYS = 180;

  private final ScheduleRepository scheduleRepository;

  @Transactional
  public void createSchedule(OpenScheduleRequestDto dto) {
    //TODO: trainer 확인해서 scheduleEntity에 연결하기

    List<ScheduleEntity> scheduleEntities = dto.toEntities();

    Set<LocalDateTime> existings = scheduleRepository.findScheduleDatesByDates(
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

  public List<ScheduleResponseDto> getScheduleList(LocalDate startDate, LocalDate endDate) {
    //TODO: 트레이니와 트레이너의 구분에 따라 다른 내용을 보여줘야 함

    LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.of(0, 0));
    LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.of(23, 59));

    if (Duration.between(startDateTime, endDateTime).toDays() > MAX_QUERY_DAYS) {
      throw new ScheduleRangeTooLong();
    }

    return scheduleRepository.getScheduleList(startDateTime, endDateTime);
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
