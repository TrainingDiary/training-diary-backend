package com.project.trainingdiary.service;

import static com.project.trainingdiary.model.type.NotificationType.RESERVATION_REGISTERED;

import com.project.trainingdiary.component.FcmPushNotification;
import com.project.trainingdiary.dto.request.schedule.OpenScheduleRequestDto;
import com.project.trainingdiary.dto.request.schedule.RegisterScheduleRequestDto;
import com.project.trainingdiary.dto.response.schedule.RegisterScheduleResponseDto;
import com.project.trainingdiary.entity.NotificationEntity;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.notification.UnsupportedNotificationTypeException;
import com.project.trainingdiary.exception.ptcontract.PtContractNotEnoughSessionException;
import com.project.trainingdiary.exception.ptcontract.PtContractNotExistException;
import com.project.trainingdiary.exception.schedule.ScheduleAlreadyExistException;
import com.project.trainingdiary.exception.schedule.ScheduleInvalidException;
import com.project.trainingdiary.exception.schedule.ScheduleNotFoundException;
import com.project.trainingdiary.exception.schedule.ScheduleStatusNotOpenException;
import com.project.trainingdiary.exception.user.UserNotFoundException;
import com.project.trainingdiary.model.NotificationMessage;
import com.project.trainingdiary.model.ScheduleDateTimes;
import com.project.trainingdiary.model.type.NotificationType;
import com.project.trainingdiary.model.type.ScheduleStatusType;
import com.project.trainingdiary.repository.NotificationRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import com.project.trainingdiary.repository.schedule.ScheduleRepository;
import com.project.trainingdiary.util.NotificationMessageGeneratorUtil;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
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
  private final NotificationRepository notificationRepository;
  private final FcmPushNotification fcmPushNotification;

  /**
   * 일정이 예약 가능하도록 열기
   */
  public void createSchedule(OpenScheduleRequestDto dto) {
    TrainerEntity trainer = getTrainer();

    List<LocalDateTime> requestedStartTimes = getRequestedTimes(dto.getDateTimes());
    Set<LocalDateTime> existings = scheduleRepository.findScheduleDatesByDates(
        trainer.getId(),
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
        .filter(s -> !s.getScheduleStatusType().equals(ScheduleStatusType.OPEN))
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
        trainer.getId(),
        getEarliest(requestedStartTimes),
        getLatest(requestedStartTimes)
    );
    existingStartTimes.retainAll(requestedStartTimes); // 요청한 것 중 존재하는 시간만 남김

    // 남은 PT 횟수가 부족하면 에러를 냄
    if (requestedStartTimes.size() > ptContract.getRemainingSession()) {
      throw new PtContractNotEnoughSessionException();
    }

    // 존재하지 않는 일정은 생성하기
    List<ScheduleEntity> newSchedules = createNewSchedules(
        requestedStartTimes, existingStartTimes, ptContract
    );

    // 이미 존재하는 일정은 기존 일정을 사용해서 업데이트
    List<ScheduleEntity> existingSchedules = updateExistingSchedules(
        trainer.getId(), existingStartTimes, ptContract
    );

    // 일정 등록
    scheduleRepository.saveAll(newSchedules);
    scheduleRepository.saveAll(existingSchedules);
    ptContractRepository.save(ptContract);

    // 알림 저장 및 전송
    NotificationEntity notification = saveNotification(
        RESERVATION_REGISTERED,
        trainer,
        ptContract.getTrainee(),
        getFirstDateOf(newSchedules, existingSchedules),
        newSchedules.size() + existingSchedules.size()
    );
    sendNotification(notification);

    return new RegisterScheduleResponseDto(
        newSchedules.size() + existingSchedules.size(),
        ptContract.getRemainingSession()
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
      long trainerId,
      Set<LocalDateTime> existingsStartTimes,
      PtContractEntity ptContract
  ) {
    return existingsStartTimes.stream()
        .map(time -> scheduleRepository.findByDates(trainerId, time, time).stream()
            .findFirst()
            .orElseThrow(ScheduleNotFoundException::new)
        )
        .peek(schedule -> {
          if (schedule.getScheduleStatusType() != ScheduleStatusType.OPEN) {
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

  private LocalDateTime getFirstDateOf(List<ScheduleEntity> newSchedules,
      List<ScheduleEntity> existingSchedules) {
    PriorityQueue<LocalDateTime> queue = new PriorityQueue<>(Comparator.naturalOrder());
    newSchedules.stream()
        .map(ScheduleEntity::getStartAt)
        .forEach(queue::add);
    existingSchedules.stream()
        .map(ScheduleEntity::getStartAt)
        .forEach(queue::add);
    return queue.peek();
  }

  /**
   * 알림 엔티티를 만들어서 저장함
   */
  private NotificationEntity saveNotification(
      NotificationType notificationType,
      TrainerEntity trainer,
      TraineeEntity trainee,
      LocalDateTime startAt,
      Integer ptSessionCount
  ) {
    NotificationMessage message = switch (notificationType) {
      case RESERVATION_REGISTERED ->
          NotificationMessageGeneratorUtil.reserveRegister(trainee.getName(), ptSessionCount);
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
