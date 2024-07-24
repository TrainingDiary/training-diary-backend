package com.project.trainingdiary.service;

import com.project.trainingdiary.component.FcmPushNotification;
import com.project.trainingdiary.dto.request.schedule.ApplyScheduleRequestDto;
import com.project.trainingdiary.dto.request.schedule.CancelScheduleByTraineeRequestDto;
import com.project.trainingdiary.dto.response.schedule.ApplyScheduleResponseDto;
import com.project.trainingdiary.dto.response.schedule.CancelScheduleByTraineeResponseDto;
import com.project.trainingdiary.dto.response.schedule.ScheduleResponseDto;
import com.project.trainingdiary.entity.NotificationEntity;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.ptcontract.PtContractNotEnoughSessionException;
import com.project.trainingdiary.exception.ptcontract.PtContractNotExistException;
import com.project.trainingdiary.exception.schedule.ScheduleNotFoundException;
import com.project.trainingdiary.exception.schedule.ScheduleRangeTooLongException;
import com.project.trainingdiary.exception.schedule.ScheduleStartIsPastException;
import com.project.trainingdiary.exception.schedule.ScheduleStartTooSoonException;
import com.project.trainingdiary.exception.schedule.ScheduleStartWithin1DayException;
import com.project.trainingdiary.exception.schedule.ScheduleStatusNotOpenException;
import com.project.trainingdiary.exception.schedule.ScheduleStatusNotReserveAppliedOrReservedException;
import com.project.trainingdiary.exception.user.UserNotFoundException;
import com.project.trainingdiary.model.type.NotificationType;
import com.project.trainingdiary.model.type.ScheduleStatusType;
import com.project.trainingdiary.repository.NotificationRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import com.project.trainingdiary.repository.schedule.ScheduleRepository;
import com.project.trainingdiary.util.NotificationMessageGeneratorUtil;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ScheduleTraineeService {

  private static final int MAX_QUERY_DAYS = 180;
  private static final LocalTime START_TIME = LocalTime.of(0, 0);
  private static final LocalTime END_TIME = LocalTime.of(23, 59);

  private final ScheduleRepository scheduleRepository;
  private final PtContractRepository ptContractRepository;
  private final TraineeRepository traineeRepository;
  private final NotificationRepository notificationRepository;
  private final FcmPushNotification fcmPushNotification;

  /**
   * 일정 예약 신청
   */
  @Transactional
  public ApplyScheduleResponseDto applySchedule(ApplyScheduleRequestDto dto,
      LocalDateTime currentTime) {
    TraineeEntity trainee = getTrainee();

    ScheduleEntity schedule = scheduleRepository.findById(dto.getScheduleId())
        .orElseThrow(ScheduleNotFoundException::new);
    // OPEN 일정이 아닌 경우 신청 불가
    if (!schedule.getScheduleStatusType().equals(ScheduleStatusType.OPEN)) {
      throw new ScheduleStatusNotOpenException();
    }
    // 과거의 일정은 신청 불가
    if (schedule.getStartAt().isBefore(currentTime)) {
      throw new ScheduleStartIsPastException();
    }
    // 1시간 내로 시작하는 일정은 신청 불가
    if (schedule.getStartAt().isBefore(currentTime.plusHours(1))) {
      throw new ScheduleStartTooSoonException();
    }

    PtContractEntity ptContract = getPtContract(
        schedule.getTrainer().getId(),
        trainee.getId()
    );
    // 남은 세션이 없는 경우 신청 불가
    if (ptContract.getRemainingSession() <= 0) {
      throw new PtContractNotEnoughSessionException();
    }

    // 일정 신청
    ptContract.useSession();
    ptContractRepository.save(ptContract);
    schedule.apply(ptContract);
    scheduleRepository.save(schedule);

    // 알림 저장 및 전송
    NotificationEntity notification = saveNotification(
        NotificationType.RESERVATION_APPLIED,
        schedule.getTrainer(),
        trainee,
        schedule.getStartAt()
    );
    sendNotification(notification);

    return new ApplyScheduleResponseDto(schedule.getId(), schedule.getScheduleStatusType());
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

    if (schedule.getScheduleStatusType().equals(ScheduleStatusType.OPEN)) {
      throw new ScheduleStatusNotReserveAppliedOrReservedException();
    }

    // 트레이니는 PT 시작 24시간 전이고, RESERVED로 확정된 일정은 취소할 수 없음
    if (schedule.getStartAt().minusDays(1).isBefore(now) &&
        (schedule.getScheduleStatusType().equals(ScheduleStatusType.RESERVED))) {
      throw new ScheduleStartWithin1DayException();
    }

    // 트레이니의 일정 취소
    // PtContract의 사용을 먼저 취소하고, schedule cancel을 해야함. cancel을 먼저하면 ptContract가 null로 변함
    PtContractEntity ptContract = schedule.getPtContract();
    ptContract.restoreSession();
    ptContractRepository.save(ptContract);
    schedule.cancel();
    scheduleRepository.save(schedule);

    // 알림 저장 및 전송
    NotificationEntity notification = saveNotification(
        NotificationType.RESERVATION_CANCELLED_BY_TRAINEE,
        schedule.getTrainer(),
        trainee,
        schedule.getStartAt()
    );
    sendNotification(notification);

    return new CancelScheduleByTraineeResponseDto(schedule.getId(),
        schedule.getScheduleStatusType());
  }

  /**
   * 트레이너의 일정 목록 조회
   */
  public List<ScheduleResponseDto> getScheduleList(LocalDate startDate, LocalDate endDate) {
    TraineeEntity trainee = getTrainee();
    PtContractEntity ptContract = getPtContract(trainee.getId());

    LocalDateTime startDateTime = LocalDateTime.of(startDate, START_TIME);
    LocalDateTime endDateTime = LocalDateTime.of(endDate, END_TIME);

    if (Duration.between(startDateTime, endDateTime).toDays() > MAX_QUERY_DAYS) {
      throw new ScheduleRangeTooLongException();
    }

    return scheduleRepository.getScheduleListByTrainee(
        ptContract.getTrainer().getId(),
        trainee.getId(),
        startDateTime,
        endDateTime
    );
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

  private PtContractEntity getPtContract(Long traineeId) {
    return ptContractRepository.findByTraineeId(traineeId)
        .orElseThrow(PtContractNotExistException::new);
  }

  /**
   * 알림 엔티티를 만들어서 저장함
   */
  private NotificationEntity saveNotification(
      NotificationType notificationType,
      TrainerEntity trainer,
      TraineeEntity trainee,
      LocalDateTime startAt
  ) {
    String message = "";
    switch (notificationType) {
      case RESERVATION_APPLIED:
        message = NotificationMessageGeneratorUtil.reserveApplied(trainee.getName(), startAt);
        break;
      case RESERVATION_CANCELLED_BY_TRAINEE:
        message = NotificationMessageGeneratorUtil.reserveCancelByTrainee(trainee.getName(),
            startAt);
        break;
      default:
        break;
    }
    NotificationEntity notification = NotificationEntity.of(
        notificationType, true, false,
        trainer, trainee, message,
        startAt.toLocalDate()
    );
    notificationRepository.save(notification);
    return notification;
  }

  /**
   * 알림을 전송하고, 전송한 사용자에게 미확인 알림 표시를 함
   */
  private void sendNotification(NotificationEntity notification) {
    fcmPushNotification.sendPushNotification(notification);
    if (notification.isToTrainee()) {
      notification.getTrainee().setUnreadNotification(true);
    }
    if (notification.isToTrainer()) {
      notification.getTrainer().setUnreadNotification(true);
    }
  }
}
