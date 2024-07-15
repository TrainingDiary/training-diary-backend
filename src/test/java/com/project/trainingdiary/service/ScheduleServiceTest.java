package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.AcceptScheduleRequestDto;
import com.project.trainingdiary.dto.request.ApplyScheduleRequestDto;
import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
import com.project.trainingdiary.dto.request.RejectScheduleRequestDto;
import com.project.trainingdiary.dto.response.ScheduleResponseDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.impl.PtContractNotExistException;
import com.project.trainingdiary.exception.impl.ScheduleAlreadyExistException;
import com.project.trainingdiary.exception.impl.ScheduleNotFoundException;
import com.project.trainingdiary.exception.impl.ScheduleRangeTooLong;
import com.project.trainingdiary.exception.impl.ScheduleStartIsPast;
import com.project.trainingdiary.exception.impl.ScheduleStartTooSoon;
import com.project.trainingdiary.exception.impl.ScheduleStatusNotOpenException;
import com.project.trainingdiary.exception.impl.ScheduleStatusNotReserveApplied;
import com.project.trainingdiary.exception.impl.UsedSessionExceededTotalSession;
import com.project.trainingdiary.model.ScheduleDateTimes;
import com.project.trainingdiary.model.ScheduleResponseDetail;
import com.project.trainingdiary.model.ScheduleStatus;
import com.project.trainingdiary.model.UserPrincipal;
import com.project.trainingdiary.model.UserRoleType;
import com.project.trainingdiary.repository.PtContractRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.schedule.ScheduleRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
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

  @Mock
  private TraineeRepository traineeRepository;

  @Mock
  private PtContractRepository ptContractRepository;

  @InjectMocks
  private ScheduleService scheduleService;

  private TrainerEntity trainer;
  private TraineeEntity trainee;
  private List<ScheduleResponseDto> responseData;

  @BeforeEach
  public void setup() {
    setupTrainee();
    setupTrainer();
    setupScheduleResponseDto();
  }

  private void setupTrainee() {
    trainee = TraineeEntity.builder()
        .id(10L)
        .email("trainee@example.com")
        .name("김트레이니")
        .role(UserRoleType.TRAINEE)
        .build();
  }

  private void setupTrainer() {
    trainer = TrainerEntity.builder()
        .id(1L)
        .email("trainer@example.com")
        .name("이트레이너")
        .role(UserRoleType.TRAINER)
        .build();
  }

  private void setupTrainerAuth() {
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

  private void setupTraineeAuth() {
    GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_TRAINEE");
    Collection authorities = Collections.singleton(authority);

    Authentication authentication = mock(Authentication.class);
    lenient().when(authentication.getAuthorities()).thenReturn(authorities);

    UserDetails userDetails = UserPrincipal.create(trainee);
    lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
    lenient().when(authentication.getName()).thenReturn(trainee.getEmail());

    SecurityContext securityContext = mock(SecurityContext.class);
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    lenient().when(traineeRepository.findByEmail(trainee.getEmail()))
        .thenReturn(Optional.of(trainee));
  }

  @AfterEach
  public void cleanup() {
    SecurityContextHolder.clearContext();
  }

  private void setupScheduleResponseDto() {
    responseData = List.of(
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
  }

  @Test
  @DisplayName("일정 열기 - 성공(6개의 일정 열기)")
  void openSchedule() {
    //given
    setupTrainerAuth();
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
    setupTrainerAuth();
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

  @Test
  @DisplayName("일정 예약 신청 - 성공")
  void applySchedule() {
    //given
    setupTraineeAuth();
    ApplyScheduleRequestDto dto = new ApplyScheduleRequestDto();
    dto.setScheduleId(100L);
    LocalDateTime currentTime = LocalDateTime.now();

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .scheduleStatus(ScheduleStatus.OPEN)
                .startAt(currentTime.plusHours(2).withMinute(0).withSecond(0).withNano(0))
                .trainer(trainer)
                .build()
        ));

    when(traineeRepository.findByEmail("trainee@example.com"))
        .thenReturn(Optional.of(trainee));

    when(ptContractRepository.findByTrainerIdAndTraineeId(1L, 10L))
        .thenReturn(Optional.of(
            PtContractEntity.builder()
                .id(1000L)
                .trainer(trainer)
                .trainee(trainee)
                .build()
        ));

    ArgumentCaptor<ScheduleEntity> captor = ArgumentCaptor.forClass(ScheduleEntity.class);
    scheduleService.applySchedule(dto, currentTime);

    //then
    verify(scheduleRepository).save(captor.capture());
    assertEquals(1000L, captor.getValue().getPtContract().getId());
  }

  @Test
  @DisplayName("일정 예약 신청 - 실패(일정이 없는 경우)")
  void applyScheduleFail_NoSchedule() {
    //given
    setupTraineeAuth();
    ApplyScheduleRequestDto dto = new ApplyScheduleRequestDto();
    dto.setScheduleId(100L);
    LocalDateTime currentTime = LocalDateTime.now();

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.empty());

    //then
    assertThrows(
        ScheduleNotFoundException.class,
        () -> scheduleService.applySchedule(dto, currentTime)
    );
  }

  @Test
  @DisplayName("일정 예약 신청 - 실패(둘이 연결된 계약이 없는 경우)")
  void applyScheduleFail_NoPtContract() {
    //given
    setupTraineeAuth();
    ApplyScheduleRequestDto dto = new ApplyScheduleRequestDto();
    dto.setScheduleId(100L);
    LocalDateTime currentTime = LocalDateTime.now();

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .scheduleStatus(ScheduleStatus.OPEN)
                .startAt(currentTime.plusHours(2).withMinute(0).withSecond(0).withNano(0))
                .trainer(trainer)
                .build()
        ));

    when(ptContractRepository.findByTrainerIdAndTraineeId(1L, 10L))
        .thenReturn(Optional.empty());

    //then
    assertThrows(
        PtContractNotExistException.class,
        () -> scheduleService.applySchedule(dto, currentTime)
    );
  }

  @Test
  @DisplayName("일정 예약 신청 - 실패(OPEN 일정이 아닌 경우)")
  void applyScheduleFail_ScheduleNotOpen() {
    //given
    setupTraineeAuth();
    ApplyScheduleRequestDto dto = new ApplyScheduleRequestDto();
    dto.setScheduleId(100L);
    LocalDateTime currentTime = LocalDateTime.now();

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .scheduleStatus(ScheduleStatus.RESERVED)
                .startAt(currentTime.plusHours(2).withMinute(0).withSecond(0).withNano(0))
                .trainer(trainer)
                .build()
        ));

    //then
    assertThrows(
        ScheduleStatusNotOpenException.class,
        () -> scheduleService.applySchedule(dto, currentTime)
    );
  }

  @Test
  @DisplayName("일정 예약 신청 - 실패(과거의 일정인 경우)")
  void applyScheduleFail_ScheduleIsPast() {
    //given
    setupTraineeAuth();
    ApplyScheduleRequestDto dto = new ApplyScheduleRequestDto();
    dto.setScheduleId(100L);
    LocalDateTime currentTime = LocalDateTime.now();

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .scheduleStatus(ScheduleStatus.OPEN)
                .startAt(currentTime.minusHours(2).withMinute(0).withSecond(0).withNano(0))
                .trainer(trainer)
                .build()
        ));

    //then
    assertThrows(
        ScheduleStartIsPast.class,
        () -> scheduleService.applySchedule(dto, currentTime)
    );
  }

  @Test
  @DisplayName("일정 예약 신청 - 실패(1시간 이내 시작하는 일정인 경우)")
  void applyScheduleFail_ScheduleTooSoon() {
    //given
    setupTraineeAuth();
    ApplyScheduleRequestDto dto = new ApplyScheduleRequestDto();
    dto.setScheduleId(100L);
    LocalDateTime currentTime = LocalDateTime.now();

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .scheduleStatus(ScheduleStatus.OPEN)
                .startAt(currentTime.plusHours(1).withMinute(0).withSecond(0).withNano(0))
                .trainer(trainer)
                .build()
        ));

    //then
    assertThrows(
        ScheduleStartTooSoon.class,
        () -> scheduleService.applySchedule(dto, currentTime)
    );
  }

  @Test
  @DisplayName("일정 수락 - 성공")
  void acceptSchedule() {
    //given
    setupTrainerAuth();
    AcceptScheduleRequestDto dto = new AcceptScheduleRequestDto();
    dto.setScheduleId(100L);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .scheduleStatus(ScheduleStatus.RESERVE_APPLIED)
                .ptContract(
                    PtContractEntity.builder()
                        .id(1000L)
                        .totalSession(10)
                        .usedSession(5)
                        .build()
                )
                .build()
        ));

    //then
    scheduleService.acceptSchedule(dto);
  }

  @Test
  @DisplayName("일정 수락 - 실패(없는 일정)")
  void acceptScheduleFail_NoSchedule() {
    //given
    setupTrainerAuth();
    AcceptScheduleRequestDto dto = new AcceptScheduleRequestDto();
    dto.setScheduleId(100L);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.empty());

    //then
    assertThrows(
        ScheduleNotFoundException.class,
        () -> scheduleService.acceptSchedule(dto)
    );
  }

  @Test
  @DisplayName("일정 수락 - 실패(아무도 신청하지 않은 예약을 수락하려 함)")
  void acceptScheduleFail_NoReserveApplied() {
    //given
    setupTrainerAuth();
    AcceptScheduleRequestDto dto = new AcceptScheduleRequestDto();
    dto.setScheduleId(100L);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .scheduleStatus(ScheduleStatus.OPEN)
                .ptContract(
                    PtContractEntity.builder()
                        .id(1000L)
                        .totalSession(10)
                        .usedSession(10) // 모두 사용
                        .build()
                )
                .build()
        ));

    //then
    assertThrows(
        ScheduleStatusNotReserveApplied.class,
        () -> scheduleService.acceptSchedule(dto)
    );
  }

  @Test
  @DisplayName("일정 수락 - 실패(연결된 PT 계약이 없음)")
  void acceptScheduleFail_NoPtContract() {
    //given
    setupTrainerAuth();
    AcceptScheduleRequestDto dto = new AcceptScheduleRequestDto();
    dto.setScheduleId(100L);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .scheduleStatus(ScheduleStatus.RESERVE_APPLIED)
                .ptContract(null)
                .build()
        ));

    //then
    assertThrows(
        PtContractNotExistException.class,
        () -> scheduleService.acceptSchedule(dto)
    );
  }

  @Test
  @DisplayName("일정 수락 - 실패(모든 세션 횟수를 사용함)")
  void acceptScheduleFail_UsedAllSession() {
    //given
    setupTrainerAuth();
    AcceptScheduleRequestDto dto = new AcceptScheduleRequestDto();
    dto.setScheduleId(100L);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .scheduleStatus(ScheduleStatus.RESERVE_APPLIED)
                .ptContract(
                    PtContractEntity.builder()
                        .id(1000L)
                        .totalSession(10)
                        .usedSession(10) // 모두 사용
                        .build()
                )
                .build()
        ));

    //then
    assertThrows(
        UsedSessionExceededTotalSession.class,
        () -> scheduleService.acceptSchedule(dto)
    );
  }

  @Test
  @DisplayName("일정 거절 - 성공")
  void rejectSchedule() {
    //given
    setupTrainerAuth();
    RejectScheduleRequestDto dto = new RejectScheduleRequestDto();
    dto.setScheduleId(100L);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .scheduleStatus(ScheduleStatus.RESERVE_APPLIED)
                .ptContract(
                    PtContractEntity.builder()
                        .id(1000L)
                        .totalSession(10)
                        .usedSession(5)
                        .build()
                )
                .build()
        ));

    ArgumentCaptor<PtContractEntity> captor = ArgumentCaptor.forClass(PtContractEntity.class);
    scheduleService.rejectSchedule(dto);

    //then
    verify(ptContractRepository).save(captor.capture());
    assertEquals(4, captor.getValue().getUsedSession());
  }

  @Test
  @DisplayName("일정 거절 - 실패(없는 일정)")
  void rejectScheduleFail_NoSchedule() {
    //given
    setupTrainerAuth();
    RejectScheduleRequestDto dto = new RejectScheduleRequestDto();
    dto.setScheduleId(100L);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.empty());

    //then
    assertThrows(
        ScheduleNotFoundException.class,
        () -> scheduleService.rejectSchedule(dto)
    );
  }

  @Test
  @DisplayName("일정 거절 - 실패(아무도 신청하지 않은 예약을 거절하려 함)")
  void rejectScheduleFail_NoReserveApplied() {
    //given
    setupTrainerAuth();
    RejectScheduleRequestDto dto = new RejectScheduleRequestDto();
    dto.setScheduleId(100L);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .scheduleStatus(ScheduleStatus.OPEN)
                .ptContract(
                    PtContractEntity.builder()
                        .id(1000L)
                        .totalSession(10)
                        .usedSession(5)
                        .build()
                )
                .build()
        ));

    //then
    assertThrows(
        ScheduleStatusNotReserveApplied.class,
        () -> scheduleService.rejectSchedule(dto)
    );
  }

  @Test
  @DisplayName("일정 거절 - 실패(연결된 PT 계약이 없음)")
  void rejectScheduleFail_NoPtContract() {
    //given
    setupTrainerAuth();
    RejectScheduleRequestDto dto = new RejectScheduleRequestDto();
    dto.setScheduleId(100L);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .scheduleStatus(ScheduleStatus.RESERVE_APPLIED)
                .ptContract(null)
                .build()
        ));

    //then
    assertThrows(
        PtContractNotExistException.class,
        () -> scheduleService.rejectSchedule(dto)
    );
  }
}