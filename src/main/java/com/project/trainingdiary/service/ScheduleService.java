package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.ApplyScheduleRequestDto;
import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
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
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.model.ScheduleDateTimes;
import com.project.trainingdiary.model.ScheduleStatus;
import com.project.trainingdiary.repository.PtContractRepository;
import com.project.trainingdiary.repository.ScheduleRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

  private static final int MAX_QUERY_DAYS = 180;

  private final ScheduleRepository scheduleRepository;
  private final PtContractRepository ptContractRepository;
  private final TraineeRepository traineeRepository;
  private final TrainerRepository trainerRepository;

  /**
   * 일정이 예약 가능하도록 열기
   */
  @Transactional
  public void createSchedule(OpenScheduleRequestDto dto) {
    TrainerEntity trainer = getTrainer();

    List<ScheduleEntity> scheduleEntities = new ArrayList<>();

    for (ScheduleDateTimes dateTime : dto.getDateTimes()) {
      LocalDate startDate = dateTime.getStartDate();
      List<LocalTime> times = dateTime.getStartTimes();

      for (LocalTime startTime : times) {
        LocalDateTime startAt = LocalDateTime.of(startDate, startTime);
        LocalDateTime endAt = startAt.plusHours(1);

        scheduleEntities.add(ScheduleEntity.builder()
            .startAt(startAt)
            .endAt(endAt)
            .trainer(trainer)
            .scheduleStatus(ScheduleStatus.OPEN)
            .build()
        );
      }
    }

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
  @Transactional
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
  @Transactional
  public void applySchedule(ApplyScheduleRequestDto dto, LocalDateTime currentTime) {
    TraineeEntity trainee = getTrainee();

    ScheduleEntity schedule = scheduleRepository.findById(dto.getScheduleId())
        .orElseThrow(ScheduleNotFoundException::new);
    if (!schedule.getScheduleStatus().equals(ScheduleStatus.OPEN)) {
      throw new ScheduleStatusNotOpenException();
    }
    // 과거의 일정은 신청 불가.
    if (schedule.getStartAt().isBefore(currentTime)) {
      throw new ScheduleStartIsPast();
    }
    // 1시간 내로 시작하는 일정은 신청 불가.
    if (schedule.getStartAt().isBefore(currentTime.plusHours(1))) {
      throw new ScheduleStartTooSoon();
    }

    PtContractEntity ptContract = ptContractRepository.findByTrainerIdAndTraineeId(
            trainee.getId(), schedule.getTrainer().getId()
        )
        .orElseThrow(PtContractNotExistException::new);

    schedule.apply(ptContract);
    scheduleRepository.save(schedule);
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
