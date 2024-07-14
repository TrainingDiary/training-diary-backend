package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.AcceptScheduleRequestDto;
import com.project.trainingdiary.dto.request.ApplyScheduleRequestDto;
import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
import com.project.trainingdiary.dto.request.RegisterScheduleRequestDto;
import com.project.trainingdiary.dto.request.RejectScheduleRequestDto;
import com.project.trainingdiary.dto.response.RegisterScheduleResponseDto;
import com.project.trainingdiary.dto.response.RejectScheduleResponseDto;
import com.project.trainingdiary.dto.response.ScheduleResponseDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.impl.PtContractNotEnoughSession;
import com.project.trainingdiary.exception.impl.PtContractNotExistException;
import com.project.trainingdiary.exception.impl.ScheduleAlreadyExistException;
import com.project.trainingdiary.exception.impl.ScheduleInvalidException;
import com.project.trainingdiary.exception.impl.ScheduleNotFoundException;
import com.project.trainingdiary.exception.impl.ScheduleRangeTooLong;
import com.project.trainingdiary.exception.impl.ScheduleStartIsPast;
import com.project.trainingdiary.exception.impl.ScheduleStartTooSoon;
import com.project.trainingdiary.exception.impl.ScheduleStatusNotOpenException;
import com.project.trainingdiary.exception.impl.ScheduleStatusNotReserveApplied;
import com.project.trainingdiary.exception.impl.UsedSessionExceededTotalSession;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.model.ScheduleDateTimes;
import com.project.trainingdiary.model.ScheduleStatus;
import com.project.trainingdiary.repository.PtContractRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.schedule.ScheduleRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ScheduleService {

  private static final int MAX_QUERY_DAYS = 180;

  private final ScheduleRepository scheduleRepository;
  private final PtContractRepository ptContractRepository;
  private final TraineeRepository traineeRepository;
  private final TrainerRepository trainerRepository;

  /**
   * 일정이 예약 가능하도록 열기
   */
  public void createSchedule(OpenScheduleRequestDto dto) {
    TrainerEntity trainer = getTrainer();

    List<LocalDateTime> requestedStartTimes = getRequestedTimes(dto.getDateTimes());
    Set<LocalDateTime> existings = scheduleRepository.findScheduleDatesByDates(
        getEarliest(requestedStartTimes),
        getLatest(requestedStartTimes)
    );

    for (LocalDateTime time : requestedStartTimes) {
      if (existings.contains(time)) {
        throw new ScheduleAlreadyExistException();
      }
    }

    List<ScheduleEntity> scheduleEntities = requestedStartTimes.stream()
        .map(startTime -> {
          LocalDateTime endAt = startTime.plusHours(1);
          return ScheduleEntity.of(startTime, endAt, trainer);
        })
        .toList();

    scheduleRepository.saveAll(scheduleEntities);
  }

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

  /**
   * 열린 일정을 닫기
   */
  public void closeSchedules(List<Long> scheduleIds) {
    // TODO: trainee는 일정을 닫을 수 없음

    List<ScheduleEntity> schedules = scheduleRepository.findAllById(scheduleIds);

    if (scheduleIds.size() != schedules.size()) {
      throw new ScheduleNotFoundException();
    }

    long notOpenSchedule = schedules.stream()
        .filter(s -> !s.getScheduleStatus().equals(ScheduleStatus.OPEN))
        .count();
    if (notOpenSchedule > 0) {
      throw new ScheduleStatusNotOpenException();
    }

    scheduleRepository.deleteAll(schedules);
  }

  /**
   * 일정 예약 신청
   */
  public void applySchedule(ApplyScheduleRequestDto dto, LocalDateTime currentTime) {
    TraineeEntity trainee = getTrainee();

    ScheduleEntity schedule = scheduleRepository.findById(dto.getScheduleId())
        .orElseThrow(ScheduleNotFoundException::new);
    // OPEN 일정이 아닌 경우 신청 불가
    if (!schedule.getScheduleStatus().equals(ScheduleStatus.OPEN)) {
      throw new ScheduleStatusNotOpenException();
    }
    // 과거의 일정은 신청 불가
    if (schedule.getStartAt().isBefore(currentTime)) {
      throw new ScheduleStartIsPast();
    }
    // 1시간 내로 시작하는 일정은 신청 불가
    if (schedule.getStartAt().isBefore(currentTime.plusHours(1))) {
      throw new ScheduleStartTooSoon();
    }

    PtContractEntity ptContract = getPtContract(
        schedule.getTrainer().getId(),
        trainee.getId()
    );

    schedule.apply(ptContract);
    scheduleRepository.save(schedule);
  }

  /**
   * 일정 예약 수락
   */
  @Transactional
  public void acceptSchedule(AcceptScheduleRequestDto dto) {
    getTrainer();

    ScheduleEntity schedule = scheduleRepository.findById(dto.getScheduleId())
        .orElseThrow(ScheduleNotFoundException::new);

    // 예약 상태가 RESERVE_APPLIED인지 확인
    if (!schedule.getScheduleStatus().equals(ScheduleStatus.RESERVE_APPLIED)) {
      throw new ScheduleStatusNotReserveApplied();
    }

    PtContractEntity ptContract = schedule.getPtContract();
    // PT 계약이 존재하지 않음
    if (ptContract == null) {
      throw new PtContractNotExistException();
    }
    // 전체 세션의 갯수와 사용한 세션의 갯수를 비교해 더 사용할 수 있는지 확인
    if (ptContract.getTotalSession() <= ptContract.getUsedSession()) {
      throw new UsedSessionExceededTotalSession();
    }

    schedule.acceptReserveApplied();
    scheduleRepository.save(schedule);

    ptContract.useSession();
    ptContractRepository.save(ptContract);
  }

  /**
   * 일정 예약 거절
   */
  @Transactional
  public RejectScheduleResponseDto rejectSchedule(RejectScheduleRequestDto dto) {
    getTrainer();

    ScheduleEntity schedule = scheduleRepository.findById(dto.getScheduleId())
        .orElseThrow(ScheduleNotFoundException::new);

    // 예약 상태가 RESERVE_APPLIED인지 확인
    if (!schedule.getScheduleStatus().equals(ScheduleStatus.RESERVE_APPLIED)) {
      throw new ScheduleStatusNotReserveApplied();
    }

    PtContractEntity ptContract = schedule.getPtContract();
    // PT 계약이 존재하지 않음
    if (ptContract == null) {
      throw new PtContractNotExistException();
    }

    schedule.rejectReserveApplied();
    scheduleRepository.save(schedule);

    ptContract.unuseSession();
    ptContractRepository.save(ptContract);

    return new RejectScheduleResponseDto(schedule.getId(), schedule.getScheduleStatus());
  }

  /**
   * 일정 확정 등록(트레이니의 신청 과정 없이 바로 등록함)
   */
  @Transactional
  public RegisterScheduleResponseDto registerSchedule(RegisterScheduleRequestDto dto) {
    TrainerEntity trainer = getTrainer();
    PtContractEntity ptContract = getPtContract(trainer.getId(), dto.getTraineeId());

    List<LocalDateTime> requestedStartTimes = getRequestedTimes(dto.getDateTimes());
    Set<LocalDateTime> existingStartTimes = scheduleRepository.findScheduleDatesByDates(
        getEarliest(requestedStartTimes),
        getLatest(requestedStartTimes)
    );
    existingStartTimes.retainAll(requestedStartTimes); // 요청한 것 중 존재하는 시간만 남김

    // 남은 PT 횟수가 부족하면 에러를 냄
    if (requestedStartTimes.size() > ptContract.getRemainSession()) {
      throw new PtContractNotEnoughSession();
    }

    // 존재하지 않는 스케쥴은 생성하기
    List<ScheduleEntity> newSchedules = createNewSchedules(
        requestedStartTimes, existingStartTimes, ptContract
    );

    // 이미 존재하는 스케쥴은 기존 스케쥴을 사용해서 업데이트
    List<ScheduleEntity> existingSchedules = updateExistingSchedules(
        existingStartTimes, ptContract
    );

    scheduleRepository.saveAll(newSchedules);
    scheduleRepository.saveAll(existingSchedules);
    ptContractRepository.save(ptContract);

    return new RegisterScheduleResponseDto(
        newSchedules.size() + existingSchedules.size(),
        ptContract.getRemainSession()
    );
  }

  private List<ScheduleEntity> createNewSchedules(
      List<LocalDateTime> requestedStartTimes,
      Set<LocalDateTime> existingsStartTimes,
      PtContractEntity ptContract
  ) {
    TrainerEntity trainer = getTrainer();
    return requestedStartTimes.stream()
        .filter(time -> !existingsStartTimes.contains(time))
        .map(startTime -> {
          LocalDateTime endAt = startTime.plusHours(1);
          return ScheduleEntity.of(startTime, endAt, trainer);
        })
        .peek(schedule -> {
          schedule.apply(ptContract);
          schedule.acceptReserveApplied();
          ptContract.useSession();
        })
        .toList();
  }

  private List<ScheduleEntity> updateExistingSchedules(
      Set<LocalDateTime> existingsStartTimes,
      PtContractEntity ptContract
  ) {
    return existingsStartTimes.stream()
        .map(time -> scheduleRepository.findByDates(time, time).stream()
            .findFirst()
            .orElseThrow(ScheduleNotFoundException::new)
        )
        .peek(schedule -> {
          if (schedule.getScheduleStatus() != ScheduleStatus.OPEN) {
            throw new ScheduleStatusNotOpenException();
          }
          schedule.apply(ptContract);
          schedule.acceptReserveApplied();
          ptContract.useSession();
        })
        .toList();
  }

  private PtContractEntity getPtContract(Long trainerId, Long traineeId) {
    return ptContractRepository.findByTrainerIdAndTraineeId(trainerId,
            traineeId)
        .orElseThrow(PtContractNotExistException::new);
  }

  private TraineeEntity getTrainee() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return traineeRepository.findByEmail(email)
        .orElseThrow(UserNotFoundException::new);
  }

  private TrainerEntity getTrainer() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return trainerRepository.findByEmail(email)
        .orElseThrow(UserNotFoundException::new);
  }

  private List<LocalDateTime> getRequestedTimes(List<ScheduleDateTimes> dateTimes) {
    return dateTimes.stream()
        .flatMap(dateTime -> dateTime.getStartTimes().stream()
            .map(startTime -> LocalDateTime.of(dateTime.getStartDate(), startTime))
        )
        .toList();
  }

  private static LocalDateTime getLatest(List<LocalDateTime> times) {
    return times.stream()
        .max(LocalDateTime::compareTo)
        .orElseThrow(ScheduleInvalidException::new);
  }

  private static LocalDateTime getEarliest(List<LocalDateTime> times) {
    return times.stream()
        .min(LocalDateTime::compareTo)
        .orElseThrow(ScheduleInvalidException::new);
  }
}
