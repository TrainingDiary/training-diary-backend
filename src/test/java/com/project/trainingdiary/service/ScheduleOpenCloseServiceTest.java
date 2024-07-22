package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.schedule.OpenScheduleRequestDto;
import com.project.trainingdiary.dto.request.schedule.RegisterScheduleRequestDto;
import com.project.trainingdiary.dto.response.schedule.RegisterScheduleResponseDto;
import com.project.trainingdiary.dto.response.schedule.ScheduleResponseDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.ptcontract.PtContractNotEnoughSession;
import com.project.trainingdiary.exception.ptcontract.PtContractNotExistException;
import com.project.trainingdiary.exception.schedule.ScheduleAlreadyExistException;
import com.project.trainingdiary.exception.schedule.ScheduleNotFoundException;
import com.project.trainingdiary.exception.schedule.ScheduleStatusNotOpenException;
import com.project.trainingdiary.model.ScheduleDateTimes;
import com.project.trainingdiary.model.ScheduleResponseDetail;
import com.project.trainingdiary.model.UserPrincipal;
import com.project.trainingdiary.model.type.ScheduleStatusType;
import com.project.trainingdiary.model.type.UserRoleType;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import com.project.trainingdiary.repository.schedule.ScheduleRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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

@DisplayName("일정 열기/닫기/등록 서비스")
@ExtendWith(MockitoExtension.class)
class ScheduleOpenCloseServiceTest {

  @Mock
  private ScheduleRepository scheduleRepository;

  @Mock
  private TrainerRepository trainerRepository;

  @Mock
  private TraineeRepository traineeRepository;

  @Mock
  private PtContractRepository ptContractRepository;

  @InjectMocks
  private ScheduleOpenCloseService scheduleOpenCloseService;

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
                    .status(ScheduleStatusType.RESERVED)
                    .build(),
                ScheduleResponseDetail.builder()
                    .startTime(LocalTime.of(11, 0))
                    .status(ScheduleStatusType.OPEN)
                    .build(),
                ScheduleResponseDetail.builder()
                    .startTime(LocalTime.of(12, 0))
                    .status(ScheduleStatusType.OPEN)
                    .build()
            ))
            .build(),
        ScheduleResponseDto.builder()
            .startDate(LocalDate.of(2024, 2, 28))
            .existReserved(false)
            .details(List.of(
                ScheduleResponseDetail.builder()
                    .startTime(LocalTime.of(20, 0))
                    .status(ScheduleStatusType.OPEN)
                    .build(),
                ScheduleResponseDetail.builder()
                    .startTime(LocalTime.of(21, 0))
                    .status(ScheduleStatusType.OPEN)
                    .build(),
                ScheduleResponseDetail.builder()
                    .startTime(LocalTime.of(22, 0))
                    .status(ScheduleStatusType.OPEN)
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

    when(scheduleTrainerService.getScheduleList(
        LocalDate.of(2024, 1, 1),
        LocalDate.of(2024, 3, 1)
    ))
        .thenReturn(responseData);

    //when
    scheduleOpenCloseService.createSchedule(dto);
    List<ScheduleResponseDto> schedules = scheduleTrainerService.getScheduleList(
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
        () -> scheduleOpenCloseService.createSchedule(dto)
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
                ScheduleEntity.builder().id(1L).scheduleStatusType(ScheduleStatusType.OPEN).build(),
                ScheduleEntity.builder().id(2L).scheduleStatusType(ScheduleStatusType.OPEN).build(),
                ScheduleEntity.builder().id(3L).scheduleStatusType(ScheduleStatusType.OPEN).build()
            )
        );

    //when
    scheduleOpenCloseService.closeSchedules(scheduleIds);
    ArgumentCaptor<List<ScheduleEntity>> captor = ArgumentCaptor.forClass(List.class);

    //then
    verify(scheduleRepository).deleteAll(captor.capture());
    assertEquals(3, captor.getValue().size());
  }

  @Test
  @DisplayName("일정 닫기 - 실패(닫으려는 일정 id 목록 중에 없는 일정이 있음)")
  void closeScheduleFail_ScheduleNotFound() {
    //given
    List<Long> scheduleIds = List.of(1L, 2L, 3L);

    //when
    when(scheduleRepository.findAllById(scheduleIds))
        .thenReturn(
            List.of(
                ScheduleEntity.builder().id(1L).scheduleStatusType(ScheduleStatusType.OPEN).build(),
                ScheduleEntity.builder().id(3L).scheduleStatusType(ScheduleStatusType.OPEN).build()
            )
        );

    //then
    assertThrows(
        ScheduleNotFoundException.class,
        () -> scheduleOpenCloseService.closeSchedules(scheduleIds)
    );
  }

  @Test
  @DisplayName("일정 닫기 - 실패(일정이 OPEN 상태가 아님)")
  void closeScheduleFail_ScheduleNotOpen() {
    //given
    List<Long> scheduleIds = List.of(1L, 2L, 3L);

    //when
    when(scheduleRepository.findAllById(scheduleIds))
        .thenReturn(
            List.of(
                ScheduleEntity.builder().id(1L).scheduleStatusType(ScheduleStatusType.OPEN).build(),
                ScheduleEntity.builder().id(2L).scheduleStatusType(ScheduleStatusType.RESERVED)
                    .build(),
                ScheduleEntity.builder().id(3L).scheduleStatusType(ScheduleStatusType.OPEN).build()
            )
        );

    //then
    assertThrows(
        ScheduleStatusNotOpenException.class,
        () -> scheduleOpenCloseService.closeSchedules(scheduleIds)
    );
  }

  @Test
  @DisplayName("일정 등록 - 성공(OPEN된 일정이 없는 경우)")
  void registerScheduleSuccess_NoOpen() {
    //given
    setupTrainerAuth();
    RegisterScheduleRequestDto dto = new RegisterScheduleRequestDto(trainee.getId(),
        dateTimesForRegister);

    //when
    when(ptContractRepository.findByTrainerIdAndTraineeId(1L, 10L))
        .thenReturn(Optional.of(
            PtContractEntity.builder()
                .id(1000L)
                .trainer(trainer)
                .trainee(trainee)
                .totalSession(10)
                .usedSession(0)
                .isTerminated(false)
                .build()
        ));

    when(scheduleRepository.findScheduleDatesByDates(
        LocalDateTime.of(2024, 1, 2, 20, 0),
        LocalDateTime.of(2024, 1, 8, 22, 0)
    ))
        .thenReturn(new HashSet<>());

    ArgumentCaptor<List<ScheduleEntity>> captorSchedule = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<PtContractEntity> captorPtContract = ArgumentCaptor.forClass(
        PtContractEntity.class);

    //then
    RegisterScheduleResponseDto response = scheduleOpenCloseService.registerSchedule(dto);

    verify(scheduleRepository, times(2)).saveAll(captorSchedule.capture());
    verify(ptContractRepository).save(captorPtContract.capture());

    assertEquals(3, captorSchedule.getAllValues().get(0).size());
    assertEquals(0, captorSchedule.getAllValues().get(1).size());
    assertEquals(3, captorPtContract.getValue().getUsedSession());
    assertEquals(7, response.getRemainingSession());
  }

  @Test
  @DisplayName("일정 등록 - 성공(이미 OPEN된 일정이 있는 경우라도 성공)")
  void registerScheduleSuccess_Open() {
    //given
    setupTrainerAuth();
    RegisterScheduleRequestDto dto = new RegisterScheduleRequestDto(trainee.getId(),
        dateTimesForRegister);

    //when
    when(ptContractRepository.findByTrainerIdAndTraineeId(1L, 10L))
        .thenReturn(Optional.of(
            PtContractEntity.builder()
                .id(1000L)
                .trainer(trainer)
                .trainee(trainee)
                .totalSession(10)
                .usedSession(0)
                .isTerminated(false)
                .build()
        ));

    when(scheduleRepository.findScheduleDatesByDates(
        LocalDateTime.of(2024, 1, 2, 20, 0),
        LocalDateTime.of(2024, 1, 8, 22, 0)
    ))
        .thenReturn(new HashSet<>(
            List.of(LocalDateTime.of(2024, 1, 2, 20, 0))
        ));

    when(scheduleRepository.findByDates(
        LocalDateTime.of(2024, 1, 2, 20, 0),
        LocalDateTime.of(2024, 1, 2, 20, 0)
    ))
        .thenReturn(
            List.of(
                ScheduleEntity.builder()
                    .id(100L)
                    .startAt(LocalDateTime.of(2024, 1, 2, 20, 0))
                    .scheduleStatusType(ScheduleStatusType.OPEN)
                    .trainer(trainer)
                    .build()
            )
        );

    ArgumentCaptor<List<ScheduleEntity>> captorSchedule = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<PtContractEntity> captorPtContract = ArgumentCaptor.forClass(
        PtContractEntity.class);

    //then
    RegisterScheduleResponseDto response = scheduleOpenCloseService.registerSchedule(dto);

    verify(scheduleRepository, times(2)).saveAll(captorSchedule.capture());
    verify(ptContractRepository).save(captorPtContract.capture());

    assertEquals(2, captorSchedule.getAllValues().get(0).size());
    assertEquals(1, captorSchedule.getAllValues().get(1).size());
    assertEquals(3, captorPtContract.getValue().getUsedSession());
    assertEquals(7, response.getRemainingSession());
  }

  @Test
  @DisplayName("일정 등록 - 실패(OPEN 상태가 아닌 일정이 이미 있는 경우)")
  void registerScheduleFail_ScheduleExistButNotOpen() {
    //given
    setupTrainerAuth();
    RegisterScheduleRequestDto dto = new RegisterScheduleRequestDto(trainee.getId(),
        dateTimesForRegister);

    //when
    when(ptContractRepository.findByTrainerIdAndTraineeId(1L, 10L))
        .thenReturn(Optional.of(
            PtContractEntity.builder()
                .id(1000L)
                .trainer(trainer)
                .trainee(trainee)
                .totalSession(10)
                .usedSession(0)
                .isTerminated(false)
                .build()
        ));

    when(scheduleRepository.findScheduleDatesByDates(
        LocalDateTime.of(2024, 1, 2, 20, 0),
        LocalDateTime.of(2024, 1, 8, 22, 0)
    ))
        .thenReturn(new HashSet<>(
            List.of(LocalDateTime.of(2024, 1, 2, 20, 0))
        ));

    when(scheduleRepository.findByDates(
        LocalDateTime.of(2024, 1, 2, 20, 0),
        LocalDateTime.of(2024, 1, 2, 20, 0)
    ))
        .thenReturn(
            List.of(
                ScheduleEntity.builder()
                    .id(100L)
                    .startAt(LocalDateTime.of(2024, 1, 2, 20, 0))
                    .scheduleStatusType(ScheduleStatusType.RESERVED)
                    .trainer(trainer)
                    .build()
            )
        );

    //then
    assertThrows(
        ScheduleStatusNotOpenException.class,
        () -> scheduleOpenCloseService.registerSchedule(dto)
    );
  }

  @Test
  @DisplayName("일정 등록 - 실패(PT 계약이 없는 경우)")
  void registerScheduleFail_NoPtContract() {
    //given
    setupTrainerAuth();
    RegisterScheduleRequestDto dto = new RegisterScheduleRequestDto(trainee.getId(),
        dateTimesForRegister);

    //when
    when(ptContractRepository.findByTrainerIdAndTraineeId(1L, 10L))
        .thenReturn(Optional.empty());

    //then
    assertThrows(
        PtContractNotExistException.class,
        () -> scheduleOpenCloseService.registerSchedule(dto)
    );
  }

  @Test
  @DisplayName("일정 등록 - 실패(PT 횟수가 부족한 경우)")
  void registerScheduleFail_SessionNotEnough() {
    //given
    setupTrainerAuth();
    RegisterScheduleRequestDto dto = new RegisterScheduleRequestDto(trainee.getId(),
        dateTimesForRegister);

    //when
    when(ptContractRepository.findByTrainerIdAndTraineeId(1L, 10L))
        .thenReturn(Optional.of(
            PtContractEntity.builder()
                .id(1000L)
                .trainer(trainer)
                .trainee(trainee)
                .totalSession(10)
                .usedSession(8)
                .isTerminated(false)
                .build()
        ));

    when(scheduleRepository.findScheduleDatesByDates(
        LocalDateTime.of(2024, 1, 2, 20, 0),
        LocalDateTime.of(2024, 1, 8, 22, 0)
    ))
        .thenReturn(new HashSet<>(
            List.of(LocalDateTime.of(2024, 1, 2, 20, 0))
        ));

    //then
    assertThrows(
        PtContractNotEnoughSession.class,
        () -> scheduleOpenCloseService.registerSchedule(dto)
    );
  }
}