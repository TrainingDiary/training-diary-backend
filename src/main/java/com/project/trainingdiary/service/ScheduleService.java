package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.response.ScheduleResponseDto;
import com.project.trainingdiary.exception.impl.ScheduleRangeTooLong;
import com.project.trainingdiary.repository.schedule.ScheduleRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ScheduleService {

  private static final int MAX_QUERY_DAYS = 180;

  private final ScheduleRepository scheduleRepository;

  /**
   * 일정 목록 조회
   */
  public List<ScheduleResponseDto> getScheduleList(LocalDate startDate, LocalDate endDate) {
    //TODO: 트레이니와 트레이너의 구분에 따라 다른 내용을 보여줘야 함

    LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.of(0, 0));
    LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.of(23, 59));

    if (Duration.between(startDateTime, endDateTime).toDays() > MAX_QUERY_DAYS) {
      throw new ScheduleRangeTooLong();
    }

    return scheduleRepository.getScheduleList(startDateTime, endDateTime);
  }
}
