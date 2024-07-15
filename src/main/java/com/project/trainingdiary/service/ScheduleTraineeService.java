package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.ApplyScheduleRequestDto;
import com.project.trainingdiary.dto.request.CancelScheduleByTraineeRequestDto;
import com.project.trainingdiary.dto.response.CancelScheduleByTraineeResponseDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.exception.impl.PtContractNotExistException;
import com.project.trainingdiary.exception.impl.ScheduleNotFoundException;
import com.project.trainingdiary.exception.impl.ScheduleStartIsPast;
import com.project.trainingdiary.exception.impl.ScheduleStartTooSoon;
import com.project.trainingdiary.exception.impl.ScheduleStartWithin1Day;
import com.project.trainingdiary.exception.impl.ScheduleStatusNotOpenException;
import com.project.trainingdiary.exception.impl.ScheduleStatusNotReserveAppliedOrReserved;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.model.ScheduleStatus;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import com.project.trainingdiary.repository.schedule.ScheduleRepository;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ScheduleTraineeService {

  private final ScheduleRepository scheduleRepository;
  private final PtContractRepository ptContractRepository;
  private final TraineeRepository traineeRepository;

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
   * 트레이니의 일정 취소
   */
  @Transactional
  public CancelScheduleByTraineeResponseDto cancelSchedule(
      CancelScheduleByTraineeRequestDto dto,
      LocalDateTime now
  ) {
    TraineeEntity trainee = getTrainee();

    ScheduleEntity schedule = scheduleRepository.findById(dto.getScheduleId())
        .filter(s -> s.getPtContract().getTrainee().equals(trainee))
        .orElseThrow(ScheduleNotFoundException::new);

    if (schedule.getScheduleStatus().equals(ScheduleStatus.OPEN)) {
      throw new ScheduleStatusNotReserveAppliedOrReserved();
    }

    // 트레이니는 PT 시작 24시간 전이고, RESERVED로 확정된 일정은 취소할 수 없음
    if (schedule.getStartAt().minusDays(1).isBefore(now) &&
        (schedule.getScheduleStatus().equals(ScheduleStatus.RESERVED))) {
      throw new ScheduleStartWithin1Day();
    }

    // PtContract의 사용을 먼저 취소하고, schedule cancel을 해야함. cancel을 먼저하면 ptContract가 null로 변함
    PtContractEntity ptContract = schedule.getPtContract();
    ptContract.unuseSession();
    ptContractRepository.save(ptContract);

    schedule.cancel();
    scheduleRepository.save(schedule);

    return new CancelScheduleByTraineeResponseDto(schedule.getId(), schedule.getScheduleStatus());
  }

  private TraineeEntity getTrainee() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return traineeRepository.findByEmail(email)
        .orElseThrow(UserNotFoundException::new);
  }

  private PtContractEntity getPtContract(Long trainerId, Long traineeId) {
    return ptContractRepository.findByTrainerIdAndTraineeId(trainerId,
            traineeId)
        .orElseThrow(PtContractNotExistException::new);
  }
}
