package com.project.trainingdiary.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
import com.project.trainingdiary.dto.request.ScheduleDateTimes;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.exception.impl.ScheduleAlreadyExistException;
import com.project.trainingdiary.service.ScheduleService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@DisplayName("일정 서비스")
@Transactional
class ScheduleServiceImplTest {

  @Autowired
  private ScheduleService scheduleService;

  @Test
  @DisplayName("일정 열기 - 성공(6개의 일정 열기)")
  void openSchedule() {
    //given
    List<ScheduleDateTimes> dateTimes = List.of(
        ScheduleDateTimes.builder()
            .startDate(LocalDate.of(2024, 1, 1))
            .startTimes(List.of(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0)
            ))
            .build(),
        ScheduleDateTimes.builder()
            .startDate(LocalDate.of(2024, 2, 28))
            .startTimes(List.of(
                LocalTime.of(20, 0),
                LocalTime.of(21, 0),
                LocalTime.of(22, 0)
            ))
            .build()
    );
    OpenScheduleRequestDto dto = OpenScheduleRequestDto.builder()
        .dateTimes(dateTimes)
        .build();

    //when
    scheduleService.createSchedule(dto);
    List<ScheduleEntity> schedules = scheduleService.getScheduleList();

    //then
    assertEquals(6, schedules.size());
    assertEquals(
        LocalDateTime.of(2024, 1, 1, 10, 0),
        schedules.stream().min(Comparator.comparing(ScheduleEntity::getStartAt)).get().getStartAt()
    );
    assertEquals(
        LocalDateTime.of(2024, 2, 28, 22, 0),
        schedules.stream().max(Comparator.comparing(ScheduleEntity::getStartAt)).get().getStartAt()
    );
  }

  @Test
  @DisplayName("일정 열기 - 실패(이미 일정이 있는 경우)")
  void openScheduleFail_AlreadyExistSchedule() {
    //given
    List<ScheduleDateTimes> dateTimes1 = List.of(
        ScheduleDateTimes.builder()
            .startDate(LocalDate.of(2024, 2, 28))
            .startTimes(List.of(LocalTime.of(20, 0)))
            .build()
    );

    List<ScheduleDateTimes> dateTimes2 = List.of(
        ScheduleDateTimes.builder()
            .startDate(LocalDate.of(2024, 1, 1))
            .startTimes(List.of(
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0)
            ))
            .build(),
        ScheduleDateTimes.builder()
            .startDate(LocalDate.of(2024, 2, 28))
            .startTimes(List.of(
                LocalTime.of(20, 0), // <- 중복된 일정이 들어가 있음
                LocalTime.of(21, 0),
                LocalTime.of(22, 0)
            ))
            .build()
    );

    OpenScheduleRequestDto dto1 = OpenScheduleRequestDto.builder()
        .dateTimes(dateTimes1)
        .build();

    OpenScheduleRequestDto dto2 = OpenScheduleRequestDto.builder()
        .dateTimes(dateTimes2)
        .build();

    //when
    scheduleService.createSchedule(dto1);

    //then
    assertThrows(ScheduleAlreadyExistException.class,
        () -> scheduleService.createSchedule(dto2));
  }
}