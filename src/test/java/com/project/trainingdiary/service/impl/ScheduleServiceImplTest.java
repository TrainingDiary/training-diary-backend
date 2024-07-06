package com.project.trainingdiary.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
import com.project.trainingdiary.dto.response.ScheduleResponseDto;
import com.project.trainingdiary.exception.impl.ScheduleAlreadyExistException;
import com.project.trainingdiary.model.ScheduleDateTimes;
import com.project.trainingdiary.model.ScheduleResponseDetail;
import com.project.trainingdiary.model.ScheduleStatus;
import com.project.trainingdiary.repository.ScheduleRepository;
import com.project.trainingdiary.service.ScheduleService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("일정 서비스")
@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplTest {

  @Mock
  private ScheduleRepository scheduleRepository;

  @InjectMocks
  private ScheduleService scheduleService;

  @BeforeEach
  void init() {

  }

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

    List<ScheduleResponseDto> responseDto = List.of(
        ScheduleResponseDto.builder()
            .startDate(LocalDate.of(2024, 1, 1))
            .existReserved(true)
            .details(List.of(
                ScheduleResponseDetail.builder()
                    .startTime(LocalTime.of(10, 0))
                    .status(ScheduleStatus.RESERVED)
                    .build(),
                ScheduleResponseDetail.builder()
                    .startTime(LocalTime.of(11, 0))
                    .status(ScheduleStatus.OPEN)
                    .build(),
                ScheduleResponseDetail.builder()
                    .startTime(LocalTime.of(12, 0))
                    .status(ScheduleStatus.OPEN)
                    .build()
            ))
            .build(),
        ScheduleResponseDto.builder()
            .startDate(LocalDate.of(2024, 2, 28))
            .existReserved(false)
            .details(List.of(
                ScheduleResponseDetail.builder()
                    .startTime(LocalTime.of(20, 0))
                    .status(ScheduleStatus.OPEN)
                    .build(),
                ScheduleResponseDetail.builder()
                    .startTime(LocalTime.of(21, 0))
                    .status(ScheduleStatus.OPEN)
                    .build(),
                ScheduleResponseDetail.builder()
                    .startTime(LocalTime.of(22, 0))
                    .status(ScheduleStatus.OPEN)
                    .build()
            ))
            .build()
    );

    when(scheduleService.getScheduleList(
        LocalDate.of(2024, 1, 1),
        LocalDate.of(2024, 3, 1)
    ))
        .thenReturn(responseDto);

    //when
    scheduleService.createSchedule(dto);
    List<ScheduleResponseDto> schedules = scheduleService.getScheduleList(
        LocalDate.of(2024, 1, 1),
        LocalDate.of(2024, 3, 1)
    );

    //then
    assertEquals(2, schedules.size());
    assertEquals(3,
        schedules.stream()
            .filter(s -> s.getStartDate().equals(LocalDate.of(2024, 2, 28)))
            .map(ScheduleResponseDto::getDetails)
            .flatMap(List::stream)
            .toList()
            .size()
    );
  }

  @Test
  @DisplayName("일정 열기 - 실패(이미 일정이 있는 경우)")
  void openScheduleFail_AlreadyExistSchedule() {
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
                LocalTime.of(20, 0), // <- 중복된 일정이 들어가 있음
                LocalTime.of(21, 0),
                LocalTime.of(22, 0)
            ))
            .build()
    );

    OpenScheduleRequestDto dto = OpenScheduleRequestDto.builder()
        .dateTimes(dateTimes)
        .build();

    when(scheduleRepository.findScheduleDatesByDates(
        eq(LocalDateTime.of(2024, 1, 1, 10, 0)),
        eq(LocalDateTime.of(2024, 2, 28, 22, 0))
    ))
        .thenReturn(Set.of(
            LocalDateTime.of(2024, 2, 28, 20, 0)
        ));

    //when
    //then
    assertThrows(
        ScheduleAlreadyExistException.class,
        () -> scheduleService.createSchedule(dto)
    );
  }
}