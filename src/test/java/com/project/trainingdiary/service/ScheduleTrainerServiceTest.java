package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.AcceptScheduleRequestDto;
import com.project.trainingdiary.dto.request.CancelScheduleByTrainerRequestDto;
import com.project.trainingdiary.dto.request.RejectScheduleRequestDto;
import com.project.trainingdiary.dto.response.CancelScheduleByTrainerResponseDto;
import com.project.trainingdiary.dto.response.ScheduleResponseDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.impl.PtContractNotExistException;
import com.project.trainingdiary.exception.impl.ScheduleNotFoundException;
import com.project.trainingdiary.exception.impl.ScheduleStatusNotReserveApplied;
import com.project.trainingdiary.exception.impl.ScheduleStatusNotReserveAppliedOrReserved;
import com.project.trainingdiary.exception.impl.UsedSessionExceededTotalSession;
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

@DisplayName("일정 트레이너 서비스")
@ExtendWith(MockitoExtension.class)
class ScheduleTrainerServiceTest {

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

  @InjectMocks
  private ScheduleTrainerService scheduleTrainerService;

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
    scheduleTrainerService.acceptSchedule(dto);
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
        () -> scheduleTrainerService.acceptSchedule(dto)
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
        () -> scheduleTrainerService.acceptSchedule(dto)
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
        () -> scheduleTrainerService.acceptSchedule(dto)
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
        () -> scheduleTrainerService.acceptSchedule(dto)
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
    scheduleTrainerService.rejectSchedule(dto);

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
        () -> scheduleTrainerService.rejectSchedule(dto)
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
        () -> scheduleTrainerService.rejectSchedule(dto)
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
        () -> scheduleTrainerService.rejectSchedule(dto)
    );
  }

  @Test
  @DisplayName("트레이너의 일정 취소 - 성공")
  void cancelScheduleByTrainer() {
    //given
    setupTrainerAuth();
    CancelScheduleByTrainerRequestDto dto = new CancelScheduleByTrainerRequestDto(100L);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .scheduleStatus(ScheduleStatus.RESERVED)
                .trainer(trainer)
                .ptContract(
                    PtContractEntity.builder()
                        .id(1000L)
                        .totalSession(10)
                        .usedSession(5)
                        .build()
                )
                .build()
        ));

    CancelScheduleByTrainerResponseDto response = scheduleTrainerService.cancelSchedule(
        dto);
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
  @DisplayName("트레이너의 일정 취소 - 실패(스케쥴이 없음)")
  void cancelScheduleByTrainerFail_NoSchedule() {
    //given
    setupTrainerAuth();
    CancelScheduleByTrainerRequestDto dto = new CancelScheduleByTrainerRequestDto(100L);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.empty());

    //then
    assertThrows(
        ScheduleNotFoundException.class,
        () -> scheduleTrainerService.cancelSchedule(dto)
    );
  }

  @Test
  @DisplayName("트레이너의 일정 취소 - 실패(일정 상태가 OPEN이라 취소할 수 없음)")
  void cancelScheduleByTrainerFail_OpenStatus() {
    //given
    setupTrainerAuth();
    CancelScheduleByTrainerRequestDto dto = new CancelScheduleByTrainerRequestDto(100L);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .scheduleStatus(ScheduleStatus.OPEN)
                .trainer(trainer)
                .ptContract(
                    PtContractEntity.builder()
                        .id(1000L)
                        .totalSession(10)
                        .usedSession(5)
                        .build()
                )
                .build())
        );

    //then
    assertThrows(
        ScheduleStatusNotReserveAppliedOrReserved.class,
        () -> scheduleTrainerService.cancelSchedule(dto)
    );
  }
}