package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.component.FcmPushNotification;
import com.project.trainingdiary.dto.request.schedule.AcceptScheduleRequestDto;
import com.project.trainingdiary.dto.request.schedule.CancelScheduleByTrainerRequestDto;
import com.project.trainingdiary.dto.request.schedule.RejectScheduleRequestDto;
import com.project.trainingdiary.dto.response.schedule.CancelScheduleByTrainerResponseDto;
import com.project.trainingdiary.dto.response.schedule.ScheduleResponseDto;
import com.project.trainingdiary.entity.NotificationEntity;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.ptcontract.PtContractNotExistException;
import com.project.trainingdiary.exception.schedule.ScheduleNotFoundException;
import com.project.trainingdiary.exception.schedule.ScheduleRangeTooLongException;
import com.project.trainingdiary.exception.schedule.ScheduleStatusNotReserveAppliedException;
import com.project.trainingdiary.exception.schedule.ScheduleStatusNotReserveAppliedOrReservedException;
import com.project.trainingdiary.model.ScheduleDateTimes;
import com.project.trainingdiary.model.ScheduleResponseDetail;
import com.project.trainingdiary.model.UserPrincipal;
import com.project.trainingdiary.model.type.NotificationType;
import com.project.trainingdiary.model.type.ScheduleStatusType;
import com.project.trainingdiary.model.type.UserRoleType;
import com.project.trainingdiary.repository.NotificationRepository;
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

@DisplayName("일정 트레이너 서비스")
@ExtendWith(MockitoExtension.class)
class ScheduleTrainerServiceTest {

  @Mock
  private ScheduleRepository scheduleRepository;

  @Mock
  private TrainerRepository trainerRepository;

  @Mock
  private PtContractRepository ptContractRepository;

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private FcmPushNotification fcmPushNotification;

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
                    .scheduleStatus(ScheduleStatusType.RESERVED)
                    .build(),
                ScheduleResponseDetail.builder()
                    .startTime(LocalTime.of(11, 0))
                    .scheduleStatus(ScheduleStatusType.OPEN)
                    .build(),
                ScheduleResponseDetail.builder()
                    .startTime(LocalTime.of(12, 0))
                    .scheduleStatus(ScheduleStatusType.OPEN)
                    .build()
            ))
            .build(),
        ScheduleResponseDto.builder()
            .startDate(LocalDate.of(2024, 2, 28))
            .existReserved(false)
            .details(List.of(
                ScheduleResponseDetail.builder()
                    .startTime(LocalTime.of(20, 0))
                    .scheduleStatus(ScheduleStatusType.OPEN)
                    .build(),
                ScheduleResponseDetail.builder()
                    .startTime(LocalTime.of(21, 0))
                    .scheduleStatus(ScheduleStatusType.OPEN)
                    .build(),
                ScheduleResponseDetail.builder()
                    .startTime(LocalTime.of(22, 0))
                    .scheduleStatus(ScheduleStatusType.OPEN)
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
    LocalDateTime startAt = LocalDateTime.of(2024, 1, 1, 0, 0, 0);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .trainer(trainer)
                .startAt(startAt)
                .scheduleStatusType(ScheduleStatusType.RESERVE_APPLIED)
                .ptContract(
                    PtContractEntity.builder()
                        .id(1000L)
                        .totalSession(10)
                        .usedSession(5)
                        .trainee(trainee)
                        .build()
                )
                .build()
        ));

    ArgumentCaptor<NotificationEntity> captorNotification = ArgumentCaptor.forClass(
        NotificationEntity.class);

    //then
    scheduleTrainerService.acceptSchedule(dto);
    verify(notificationRepository).save(captorNotification.capture());
    assertEquals(NotificationType.RESERVATION_ACCEPTED,
        captorNotification.getValue().getNotificationType());
    assertTrue(captorNotification.getValue().isToTrainee());
    assertFalse(captorNotification.getValue().isToTrainer());
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
                .scheduleStatusType(ScheduleStatusType.OPEN)
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
        ScheduleStatusNotReserveAppliedException.class,
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
                .scheduleStatusType(ScheduleStatusType.RESERVE_APPLIED)
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
  @DisplayName("일정 거절 - 성공")
  void rejectSchedule() {
    //given
    setupTrainerAuth();
    RejectScheduleRequestDto dto = new RejectScheduleRequestDto();
    dto.setScheduleId(100L);
    LocalDateTime startAt = LocalDateTime.of(2024, 1, 1, 0, 0, 0);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .scheduleStatusType(ScheduleStatusType.RESERVE_APPLIED)
                .startAt(startAt)
                .trainer(trainer)
                .ptContract(
                    PtContractEntity.builder()
                        .id(1000L)
                        .totalSession(10)
                        .usedSession(5)
                        .trainee(trainee)
                        .build()
                )
                .build()
        ));

    ArgumentCaptor<PtContractEntity> captorPtContract = ArgumentCaptor.forClass(
        PtContractEntity.class);
    ArgumentCaptor<NotificationEntity> captorNotification = ArgumentCaptor.forClass(
        NotificationEntity.class);
    scheduleTrainerService.rejectSchedule(dto);

    //then
    verify(ptContractRepository).save(captorPtContract.capture());
    verify(notificationRepository).save(captorNotification.capture());
    assertEquals(4, captorPtContract.getValue().getUsedSession());
    assertEquals(NotificationType.RESERVATION_REJECTED,
        captorNotification.getValue().getNotificationType());
    assertTrue(captorNotification.getValue().isToTrainee());
    assertFalse(captorNotification.getValue().isToTrainer());
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
                .scheduleStatusType(ScheduleStatusType.OPEN)
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
        ScheduleStatusNotReserveAppliedException.class,
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
                .scheduleStatusType(ScheduleStatusType.RESERVE_APPLIED)
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
    LocalDateTime startAt = LocalDateTime.of(2024, 1, 1, 0, 0, 0);

    //when
    when(scheduleRepository.findById(100L))
        .thenReturn(Optional.of(
            ScheduleEntity.builder()
                .id(100L)
                .scheduleStatusType(ScheduleStatusType.RESERVED)
                .trainer(trainer)
                .startAt(startAt)
                .ptContract(
                    PtContractEntity.builder()
                        .id(1000L)
                        .totalSession(10)
                        .usedSession(5)
                        .trainee(trainee)
                        .build()
                )
                .build()
        ));

    CancelScheduleByTrainerResponseDto response = scheduleTrainerService.cancelSchedule(
        dto);
    ArgumentCaptor<PtContractEntity> captorPtContract = ArgumentCaptor.forClass(
        PtContractEntity.class);
    ArgumentCaptor<ScheduleEntity> captorSchedule = ArgumentCaptor.forClass(ScheduleEntity.class);
    ArgumentCaptor<NotificationEntity> captorNotification = ArgumentCaptor.forClass(
        NotificationEntity.class);

    //then
    verify(ptContractRepository).save(captorPtContract.capture());
    verify(scheduleRepository).save(captorSchedule.capture());

    assertEquals(4, captorPtContract.getValue().getUsedSession());
    assertEquals(ScheduleStatusType.OPEN, captorSchedule.getValue().getScheduleStatusType());
    assertEquals(ScheduleStatusType.OPEN, response.getScheduleStatus());
    verify(notificationRepository).save(captorNotification.capture());
    assertEquals(NotificationType.RESERVATION_CANCELLED_BY_TRAINER,
        captorNotification.getValue().getNotificationType());
    assertTrue(captorNotification.getValue().isToTrainee());
    assertFalse(captorNotification.getValue().isToTrainer());
  }

  @Test
  @DisplayName("트레이너의 일정 취소 - 실패(일정이 없음)")
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
                .scheduleStatusType(ScheduleStatusType.OPEN)
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
        ScheduleStatusNotReserveAppliedOrReservedException.class,
        () -> scheduleTrainerService.cancelSchedule(dto)
    );
  }

  @Test
  @DisplayName("일정 목록 조회 - 성공")
  void getScheduleList() {
    setupTrainerAuth();

    when(scheduleRepository.getScheduleListByTrainer(
        eq(1L),
        eq(LocalDateTime.of(2024, 1, 1, 0, 0)),
        eq(LocalDateTime.of(2024, 3, 1, 23, 59))
    ))
        .thenReturn(responseData);

    scheduleTrainerService.getScheduleList(
        LocalDate.of(2024, 1, 1),
        LocalDate.of(2024, 3, 1)
    );
  }

  @Test
  @DisplayName("일정 목록 조회 - 실패(조회 범위가 너무 큰 경우)")
  void getScheduleListFail_RangeTooLong() {
    setupTrainerAuth();

    assertThrows(
        ScheduleRangeTooLongException.class,
        () -> scheduleTrainerService.getScheduleList(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 9, 1)
        )
    );
  }
}