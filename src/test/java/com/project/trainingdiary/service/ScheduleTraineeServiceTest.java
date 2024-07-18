package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.ApplyScheduleRequestDto;
import com.project.trainingdiary.dto.request.CancelScheduleByTraineeRequestDto;
import com.project.trainingdiary.dto.response.CancelScheduleByTraineeResponseDto;
import com.project.trainingdiary.dto.response.ScheduleResponseDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.impl.PtContractNotExistException;
import com.project.trainingdiary.exception.impl.ScheduleNotFoundException;
import com.project.trainingdiary.exception.impl.ScheduleStartIsPast;
import com.project.trainingdiary.exception.impl.ScheduleStartTooSoon;
import com.project.trainingdiary.exception.impl.ScheduleStartWithin1Day;
import com.project.trainingdiary.exception.impl.ScheduleStatusNotOpenException;
import com.project.trainingdiary.exception.impl.ScheduleStatusNotReserveAppliedOrReserved;
import com.project.trainingdiary.model.ScheduleDateTimes;
import com.project.trainingdiary.model.ScheduleResponseDetail;
import com.project.trainingdiary.model.ScheduleStatus;
import com.project.trainingdiary.model.UserPrincipal;
import com.project.trainingdiary.model.UserRoleType;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import com.project.trainingdiary.repository.schedule.ScheduleRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

@DisplayName("일정 트레이니 서비스")
@ExtendWith(MockitoExtension.class)
class ScheduleTraineeServiceTest {

  @Mock
  private ScheduleRepository scheduleRepository;

  @Mock
  private TrainerRepository trainerRepository;

  @Mock
  private TraineeRepository traineeRepository;

  @Mock
  private PtContractRepository ptContractRepository;

  @InjectMocks
  private ScheduleTraineeService scheduleTraineeService;

  private TrainerEntity trainer;
  private TraineeEntity trainee;
  private List<ScheduleResponseDto> responseData;
  private List<ScheduleDateTimes> dateTimesForRegister;

  @BeforeEach
  public void setup() {
    setupDateTimes();
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

  private void setupDateTimes() {
    dateTimesForRegister = List.of(
        ScheduleDateTimes.builder()
            .startDate(LocalDate.of(2024, 1, 2))
            .startTimes(List.of(
                LocalTime.of(20, 0)
            ))
            .build(),
        ScheduleDateTimes.builder()
            .startDate(LocalDate.of(2024, 1, 5))
            .startTimes(List.of(
                LocalTime.of(20, 0)
            ))
            .build(),
        ScheduleDateTimes.builder()
            .startDate(LocalDate.of(2024, 1, 8))
            .startTimes(List.of(
                LocalTime.of(22, 0)
            ))
            .build()
    );
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
    scheduleTraineeService.applySchedule(dto, currentTime);

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
        () -> scheduleTraineeService.applySchedule(dto, currentTime)
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
        () -> scheduleTraineeService.applySchedule(dto, currentTime)
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
        () -> scheduleTraineeService.applySchedule(dto, currentTime)
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
        () -> scheduleTraineeService.applySchedule(dto, currentTime)
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
        () -> scheduleTraineeService.applySchedule(dto, currentTime)
    );
  }

  @Test
  @DisplayName("트레이니의 일정 취소 - 성공")
  void cancelScheduleByTrainee() {
    //given
    setupTraineeAuth();
    CancelScheduleByTraineeRequestDto dto = new CancelScheduleByTraineeRequestDto(100L);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .startAt(LocalDateTime.of(2024, 7, 16, 9, 0, 0))
                .scheduleStatus(ScheduleStatus.RESERVED)
                .trainer(trainer)
                .ptContract(
                    PtContractEntity.builder()
                        .id(1000L)
                        .totalSession(10)
                        .usedSession(5)
                        .trainer(trainer)
                        .trainee(trainee)
                        .build()
                )
                .build()
        ));

    CancelScheduleByTraineeResponseDto response = scheduleTraineeService.cancelSchedule(
        dto,
        LocalDateTime.of(2024, 7, 15, 8, 0, 0)
    );
    ArgumentCaptor<PtContractEntity> ptContractCaptor = ArgumentCaptor.forClass(
        PtContractEntity.class);
    ArgumentCaptor<ScheduleEntity> scheduleCaptor = ArgumentCaptor.forClass(ScheduleEntity.class);

    //then
    verify(ptContractRepository).save(ptContractCaptor.capture());
    verify(scheduleRepository).save(scheduleCaptor.capture());

    assertEquals(4, ptContractCaptor.getValue().getUsedSession());
    assertEquals(ScheduleStatus.OPEN, scheduleCaptor.getValue().getScheduleStatus());
    assertEquals(ScheduleStatus.OPEN, response.getScheduleStatus());
  }

  @Test
  @DisplayName("트레이니의 일정 취소 - 실패(일정이 없음)")
  void cancelScheduleByTraineeFail_NoSchedule() {
    //given
    setupTraineeAuth();
    CancelScheduleByTraineeRequestDto dto = new CancelScheduleByTraineeRequestDto(100L);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.empty());

    //then
    assertThrows(
        ScheduleNotFoundException.class,
        () -> scheduleTraineeService.cancelSchedule(
            dto,
            LocalDateTime.of(2024, 7, 15, 8, 0, 0)
        ));
  }

  @Test
  @DisplayName("트레이니의 일정 취소 - 실패(일정 상태가 OPEN이라 취소할 수 없음)")
  void cancelScheduleByTraineeFail_OpenStatus() {
    //given
    setupTraineeAuth();
    CancelScheduleByTraineeRequestDto dto = new CancelScheduleByTraineeRequestDto(100L);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(ScheduleEntity.builder()
            .id(100L)
            .scheduleStatus(ScheduleStatus.OPEN)
            .trainer(trainer)
            .ptContract(
                PtContractEntity.builder()
                    .id(1000L)
                    .trainer(trainer)
                    .trainee(trainee)
                    .totalSession(10)
                    .usedSession(5)
                    .build()
            )
            .build())
        );

    //then
    assertThrows(
        ScheduleStatusNotReserveAppliedOrReserved.class,
        () -> scheduleTraineeService.cancelSchedule(
            dto,
            LocalDateTime.of(2024, 7, 15, 8, 0, 0)
        ));
  }

  @Test
  @DisplayName("트레이니의 일정 취소 - 실패(RESERVED 상태의 일정이 24시간 미만으로 남았음)")
  void cancelScheduleByTraineeFail_ReservedWithin1Day() {
    //given
    setupTraineeAuth();
    CancelScheduleByTraineeRequestDto dto = new CancelScheduleByTraineeRequestDto(100L);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(ScheduleEntity.builder()
            .id(100L)
            .startAt(LocalDateTime.of(2024, 7, 16, 7, 0, 0)) // 내일 아침 7시 예약
            .scheduleStatus(ScheduleStatus.RESERVED)
            .trainer(trainer)
            .ptContract(
                PtContractEntity.builder()
                    .id(1000L)
                    .trainer(trainer)
                    .trainee(trainee)
                    .totalSession(10)
                    .usedSession(5)
                    .build()
            )
            .build())
        );

    //then
    assertThrows(
        ScheduleStartWithin1Day.class,
        () -> scheduleTraineeService.cancelSchedule(
            dto,
            LocalDateTime.of(2024, 7, 15, 8, 0, 0) // 전날 아침 8시에 취소하려함
        ));
  }
}