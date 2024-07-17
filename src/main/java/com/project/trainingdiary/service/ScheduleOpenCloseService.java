package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
import com.project.trainingdiary.dto.request.RegisterScheduleRequestDto;
import com.project.trainingdiary.dto.response.RegisterScheduleResponseDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.impl.PtContractNotEnoughSession;
import com.project.trainingdiary.exception.impl.PtContractNotExistException;
import com.project.trainingdiary.exception.impl.ScheduleAlreadyExistException;
import com.project.trainingdiary.exception.impl.ScheduleInvalidException;
import com.project.trainingdiary.exception.impl.ScheduleNotFoundException;
import com.project.trainingdiary.exception.impl.ScheduleStatusNotOpenException;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.model.ScheduleDateTimes;
import com.project.trainingdiary.model.ScheduleStatus;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import com.project.trainingdiary.repository.schedule.ScheduleRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ScheduleOpenCloseService {

  private final ScheduleRepository scheduleRepository;
  private final PtContractRepository ptContractRepository;
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
   * 열린 일정을 닫기
   */
  public void closeSchedules(List<Long> scheduleIds) {
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

  private TrainerEntity getTrainer() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return trainerRepository.findByEmail(email)
        .orElseThrow(UserNotFoundException::new);
  }

  private PtContractEntity getPtContract(Long trainerId, Long traineeId) {
    return ptContractRepository.findByTrainerIdAndTraineeId(trainerId,
            traineeId)
        .orElseThrow(PtContractNotExistException::new);
  }

  private List<LocalDateTime> getRequestedTimes(List<ScheduleDateTimes> dateTimes) {
    return dateTimes.stream()
        .flatMap(dateTime -> dateTime.getStartTimes().stream()
            .map(startTime -> LocalDateTime.of(dateTime.getStartDate(), startTime))
        )
        .toList();
  }

  private LocalDateTime getEarliest(List<LocalDateTime> times) {
    return times.stream()
        .min(LocalDateTime::compareTo)
        .orElseThrow(ScheduleInvalidException::new);
  }

  private LocalDateTime getLatest(List<LocalDateTime> times) {
    return times.stream()
        .max(LocalDateTime::compareTo)
        .orElseThrow(ScheduleInvalidException::new);
  }
}
