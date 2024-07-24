package com.project.trainingdiary.service;

import com.project.trainingdiary.component.FcmPushNotification;
import com.project.trainingdiary.dto.request.schedule.AcceptScheduleRequestDto;
import com.project.trainingdiary.dto.request.schedule.CancelScheduleByTrainerRequestDto;
import com.project.trainingdiary.dto.request.schedule.RejectScheduleRequestDto;
import com.project.trainingdiary.dto.response.schedule.CancelScheduleByTrainerResponseDto;
import com.project.trainingdiary.dto.response.schedule.RejectScheduleResponseDto;
import com.project.trainingdiary.dto.response.schedule.ScheduleResponseDto;
import com.project.trainingdiary.entity.NotificationEntity;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.notification.UnsupportedNotificationTypeException;
import com.project.trainingdiary.exception.ptcontract.PtContractNotExistException;
import com.project.trainingdiary.exception.ptcontract.UsedSessionExceededTotalSessionException;
import com.project.trainingdiary.exception.schedule.ScheduleNotFoundException;
import com.project.trainingdiary.exception.schedule.ScheduleRangeTooLongException;
import com.project.trainingdiary.exception.schedule.ScheduleStatusNotReserveAppliedException;
import com.project.trainingdiary.exception.schedule.ScheduleStatusNotReserveAppliedOrReservedException;
import com.project.trainingdiary.exception.user.UserNotFoundException;
import com.project.trainingdiary.model.NotificationMessage;
import com.project.trainingdiary.model.type.NotificationType;
import com.project.trainingdiary.model.type.ScheduleStatusType;
import com.project.trainingdiary.repository.NotificationRepository;
import com.project.trainingdiary.repository.TrainerRepository;
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
public class ScheduleTrainerService {

  private static final int MAX_QUERY_DAYS = 180;
  private static final LocalTime START_TIME = LocalTime.of(0, 0);
  private static final LocalTime END_TIME = LocalTime.of(23, 59);

  private final ScheduleRepository scheduleRepository;
  private final PtContractRepository ptContractRepository;
  private final TrainerRepository trainerRepository;
  private final NotificationRepository notificationRepository;
  private final FcmPushNotification fcmPushNotification;

  /**
   * 일정 예약 수락
   */
  @Transactional
  public void acceptSchedule(AcceptScheduleRequestDto dto) {
    getTrainer();

    ScheduleEntity schedule = scheduleRepository.findById(dto.getScheduleId())
        .orElseThrow(ScheduleNotFoundException::new);

    // 예약 상태가 RESERVE_APPLIED인지 확인
    if (!schedule.getScheduleStatusType().equals(ScheduleStatusType.RESERVE_APPLIED)) {
      throw new ScheduleStatusNotReserveAppliedException();
    }

    PtContractEntity ptContract = schedule.getPtContract();
    // PT 계약이 존재하지 않음
    if (ptContract == null) {
      throw new PtContractNotExistException();
    }
    // 전체 세션의 갯수와 사용한 세션의 갯수를 비교해 더 사용할 수 있는지 확인
    if (ptContract.getTotalSession() <= ptContract.getUsedSession()) {
      throw new UsedSessionExceededTotalSessionException();
    }

    // 일정 수락
    schedule.acceptReserveApplied();
    scheduleRepository.save(schedule);

    // 알림 저장 및 전송
    NotificationEntity notification = saveNotification(
        NotificationType.RESERVATION_ACCEPTED,
        schedule.getTrainer(),
        ptContract.getTrainee(),
        schedule.getStartAt()
    );
    sendNotification(notification);
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
    if (!schedule.getScheduleStatusType().equals(ScheduleStatusType.RESERVE_APPLIED)) {
      throw new ScheduleStatusNotReserveAppliedException();
    }

    PtContractEntity ptContract = schedule.getPtContract();
    // PT 계약이 존재하지 않음
    if (ptContract == null) {
      throw new PtContractNotExistException();
    }

    // 일정 거절
    schedule.rejectReserveApplied();
    scheduleRepository.save(schedule);
    ptContract.restoreSession();
    ptContractRepository.save(ptContract);

    // 알림 저장 및 전송
    NotificationEntity notification = saveNotification(
        NotificationType.RESERVATION_REJECTED,
        schedule.getTrainer(),
        ptContract.getTrainee(),
        schedule.getStartAt()
    );
    sendNotification(notification);

    return new RejectScheduleResponseDto(schedule.getId(), schedule.getScheduleStatusType());
  }

  /**
   * 트레이너의 일정 취소
   */
  @Transactional
  public CancelScheduleByTrainerResponseDto cancelSchedule(
      CancelScheduleByTrainerRequestDto dto
  ) {
    TrainerEntity trainer = getTrainer();

    ScheduleEntity schedule = scheduleRepository.findById(dto.getScheduleId())
        .filter(s -> s.getTrainer().equals(trainer))
        .orElseThrow(ScheduleNotFoundException::new);

    if (schedule.getScheduleStatusType().equals(ScheduleStatusType.OPEN)) {
      throw new ScheduleStatusNotReserveAppliedOrReservedException();
    }

    // 일정 취소
    // PtContract의 사용을 먼저 취소하고, schedule cancel을 해야함. cancel을 먼저하면 ptContract가 null로 변함
    PtContractEntity ptContract = schedule.getPtContract();
    ptContract.restoreSession();
    ptContractRepository.save(ptContract);
    schedule.cancel();
    scheduleRepository.save(schedule);

    // 알림 저장 및 전송
    NotificationEntity notification = saveNotification(
        NotificationType.RESERVATION_CANCELLED_BY_TRAINER,
        schedule.getTrainer(),
        ptContract.getTrainee(),
        schedule.getStartAt()
    );
    sendNotification(notification);

    return new CancelScheduleByTrainerResponseDto(schedule.getId(),
        schedule.getScheduleStatusType());
  }

  /**
   * 트레이너의 일정 목록 조회
   */
  public List<ScheduleResponseDto> getScheduleList(LocalDate startDate, LocalDate endDate) {
    TrainerEntity trainer = getTrainer();

    LocalDateTime startDateTime = LocalDateTime.of(startDate, START_TIME);
    LocalDateTime endDateTime = LocalDateTime.of(endDate, END_TIME);

    if (Duration.between(startDateTime, endDateTime).toDays() > MAX_QUERY_DAYS) {
      throw new ScheduleRangeTooLongException();
    }

    return scheduleRepository.getScheduleListByTrainer(trainer.getId(), startDateTime, endDateTime);
  }

  private TrainerEntity getTrainer() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return trainerRepository.findByEmail(email)
        .orElseThrow(UserNotFoundException::new);
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
    NotificationMessage message = switch (notificationType) {
      case RESERVATION_ACCEPTED ->
          NotificationMessageGeneratorUtil.reserveAccept(trainer.getName(), startAt);
      case RESERVATION_REJECTED ->
          NotificationMessageGeneratorUtil.reserveReject(trainer.getName(), startAt);
      case RESERVATION_CANCELLED_BY_TRAINER ->
          NotificationMessageGeneratorUtil.reserveCancelByTrainer(trainer.getName(),
              startAt);
      default -> throw new UnsupportedNotificationTypeException();
    };
    NotificationEntity notification = NotificationEntity.of(
        notificationType, false, true,
        trainer, trainee, message.getBody(), message.getTitle(),
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
