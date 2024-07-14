package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.AcceptScheduleRequestDto;
import com.project.trainingdiary.dto.request.ApplyScheduleRequestDto;
import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
import com.project.trainingdiary.dto.request.RejectScheduleRequestDto;
import com.project.trainingdiary.dto.response.RejectScheduleResponseDto;
import com.project.trainingdiary.dto.response.ScheduleResponseDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
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
import com.project.trainingdiary.model.ScheduleStatus;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import com.project.trainingdiary.repository.schedule.ScheduleRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

    List<ScheduleEntity> scheduleEntities = dto.getDateTimes().stream()
        .flatMap(dateTime -> dateTime.getStartTimes().stream()
            .map(startTime -> {
              LocalDate startDate = dateTime.getStartDate();
              LocalDateTime startAt = LocalDateTime.of(startDate, startTime);
              LocalDateTime endAt = startAt.plusHours(1);
              return ScheduleEntity.of(startAt, endAt, trainer);
            }))
        .collect(Collectors.toList());

    Set<LocalDateTime> existings = scheduleRepository.findScheduleDatesByDates(
        getEarliest(scheduleEntities),
        getLatest(scheduleEntities)
    );

    for (ScheduleEntity schedule : scheduleEntities) {
      if (existings.contains(schedule.getStartAt())) {
        throw new ScheduleAlreadyExistException();
      }
    }

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

    PtContractEntity ptContract = ptContractRepository.findByTrainerIdAndTraineeId(
            schedule.getTrainer().getId(), trainee.getId()
        )
        .orElseThrow(PtContractNotExistException::new);

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

  private static LocalDateTime getLatest(List<ScheduleEntity> scheduleEntities) {
    return scheduleEntities.stream()
        .max(Comparator.comparing(ScheduleEntity::getStartAt))
        .map(ScheduleEntity::getStartAt)
        .orElseThrow(ScheduleInvalidException::new);
  }

  private static LocalDateTime getEarliest(List<ScheduleEntity> scheduleEntities) {
    return scheduleEntities.stream()
        .min(Comparator.comparing(ScheduleEntity::getStartAt))
        .map(ScheduleEntity::getStartAt)
        .orElseThrow(ScheduleInvalidException::new);
  }
}
