package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
import com.project.trainingdiary.dto.response.ScheduleResponseDto;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.impl.ScheduleAlreadyExistException;
import com.project.trainingdiary.exception.impl.ScheduleNotFoundException;
import com.project.trainingdiary.exception.impl.ScheduleRangeTooLong;
import com.project.trainingdiary.exception.impl.ScheduleStatusNotOpenException;
import com.project.trainingdiary.model.ScheduleDateTimes;
import com.project.trainingdiary.model.ScheduleResponseDetail;
import com.project.trainingdiary.model.ScheduleStatus;
import com.project.trainingdiary.model.UserPrincipal;
import com.project.trainingdiary.model.UserRoleType;
import com.project.trainingdiary.repository.ScheduleRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@DisplayName("일정 서비스")
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

  @Mock
  private ScheduleRepository scheduleRepository;

  @Mock
  private TrainerRepository trainerRepository;

  @InjectMocks
  private ScheduleService scheduleService;

  private TrainerEntity trainer;
  private TraineeEntity trainee;

  @BeforeEach
  public void setup() {
    setupTrainee();
    setupTrainer();
  }

  private void setupTrainee() {
    trainee = new TraineeEntity();
    trainee.setId(10L);
    trainee.setEmail("trainee@example.com");
    trainee.setName("김트레이니");
    trainee.setRole(UserRoleType.TRAINEE);
  }

  private void setupTrainer() {
    trainer = TrainerEntity.builder()
        .id(1L)
        .email("trainer@example.com")
        .name("이트레이너")
        .role(UserRoleType.TRAINER)
        .build();

    // 트레이너의 인증정보가 들어있는 상태
    GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_TRAINER");
    Collection authorities = Collections.singleton(authority);

    Authentication authentication = mock(Authentication.class);
    lenient().when(authentication.getAuthorities()).thenReturn(authorities);

    UserDetails userDetails = UserPrincipal.create(trainer);
    lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
    lenient().when(authentication.getName()).thenReturn(trainer.getEmail());

    SecurityContext securityContext = mock(SecurityContext.class);
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    lenient().when(trainerRepository.findByEmail(trainer.getEmail()))
        .thenReturn(Optional.of(trainer));
  }


  List<ScheduleResponseDto> responseData = List.of(
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

    when(scheduleService.getScheduleList(
        LocalDate.of(2024, 1, 1),
        LocalDate.of(2024, 3, 1)
    ))
        .thenReturn(responseData);

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

  @Test
  @DisplayName("일정 목록 조회 - 성공")
  void getScheduleList() {
    when(scheduleRepository.getScheduleList(
        eq(LocalDateTime.of(2024, 1, 1, 0, 0)),
        eq(LocalDateTime.of(2024, 3, 1, 23, 59))
    ))
        .thenReturn(responseData);

    scheduleService.getScheduleList(
        LocalDate.of(2024, 1, 1),
        LocalDate.of(2024, 3, 1)
    );
  }

  @Test
  @DisplayName("일정 목록 조회 - 실패(조회 범위가 너무 큰 경우)")
  void getScheduleListFail_RangeTooLong() {
    assertThrows(
        ScheduleRangeTooLong.class,
        () -> scheduleService.getScheduleList(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 9, 1)
        )
    );
  }

  @Test
  @DisplayName("일정 닫기 - 성공")
  void closeSchedule() {
    //given
    List<Long> scheduleIds = List.of(1L, 2L, 3L);
    when(scheduleRepository.findAllById(scheduleIds))
        .thenReturn(
            List.of(
                ScheduleEntity.builder().id(1L).scheduleStatus(ScheduleStatus.OPEN).build(),
                ScheduleEntity.builder().id(2L).scheduleStatus(ScheduleStatus.OPEN).build(),
                ScheduleEntity.builder().id(3L).scheduleStatus(ScheduleStatus.OPEN).build()
            )
        );

    //when
    scheduleService.closeSchedules(scheduleIds);
    ArgumentCaptor<List<ScheduleEntity>> captor = ArgumentCaptor.forClass(List.class);

    //then
    verify(scheduleRepository).deleteAll(captor.capture());
    assertEquals(3, captor.getValue().size());
  }

  @Test
  @DisplayName("일정 닫기 - 실패(스케쥴 아이디 목록 중에 없는 스케쥴이 있음)")
  void closeScheduleFail_ScheduleNotFound() {
    //given
    List<Long> scheduleIds = List.of(1L, 2L, 3L);

    //when
    when(scheduleRepository.findAllById(scheduleIds))
        .thenReturn(
            List.of(
                ScheduleEntity.builder().id(1L).scheduleStatus(ScheduleStatus.OPEN).build(),
                ScheduleEntity.builder().id(3L).scheduleStatus(ScheduleStatus.OPEN).build()
            )
        );

    //then
    assertThrows(
        ScheduleNotFoundException.class,
        () -> scheduleService.closeSchedules(scheduleIds)
    );
  }

  @Test
  @DisplayName("일정 닫기 - 실패(스케쥴이 OPEN 상태가 아님)")
  void closeScheduleFail_ScheduleNotOpen() {
    //given
    List<Long> scheduleIds = List.of(1L, 2L, 3L);

    //when
    when(scheduleRepository.findAllById(scheduleIds))
        .thenReturn(
            List.of(
                ScheduleEntity.builder().id(1L).scheduleStatus(ScheduleStatus.OPEN).build(),
                ScheduleEntity.builder().id(2L).scheduleStatus(ScheduleStatus.RESERVED).build(),
                ScheduleEntity.builder().id(3L).scheduleStatus(ScheduleStatus.OPEN).build()
            )
        );

    //then
    assertThrows(
        ScheduleStatusNotOpenException.class,
        () -> scheduleService.closeSchedules(scheduleIds)
    );
  }
}