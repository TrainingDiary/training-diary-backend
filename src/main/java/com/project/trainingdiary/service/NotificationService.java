package com.project.trainingdiary.service;

import com.project.trainingdiary.component.FcmPushNotification;
import com.project.trainingdiary.dto.response.notification.NotificationResponseDto;
import com.project.trainingdiary.entity.NotificationEntity;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.user.UserNotFoundException;
import com.project.trainingdiary.model.type.NotificationType;
import com.project.trainingdiary.model.type.UserRoleType;
import com.project.trainingdiary.repository.NotificationRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.schedule.ScheduleRepository;
import com.project.trainingdiary.util.NotificationMessageGeneratorUtil;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
  private final NotificationRepository notificationRepository;
  private final TrainerRepository trainerRepository;
  private final TraineeRepository traineeRepository;
  private final ScheduleRepository scheduleRepository;
  private final FcmPushNotification fcmPushNotification;

  /**
   * 알림 목록 조회
   */
  public Page<NotificationResponseDto> getNotificationList(Pageable pageable) {
    switch (getMyRole()) {
      case TRAINEE -> {
        TraineeEntity trainee = getTrainee();
        trainee.setUnreadNotification(false);
        traineeRepository.save(trainee);
        return notificationRepository.findByTrainee_Id(trainee.getId(), pageable)
            .map(NotificationResponseDto::fromEntity);
      }
      case TRAINER -> {
        TrainerEntity trainer = getTrainer();
        trainer.setUnreadNotification(false);
        trainerRepository.save(trainer);
        return notificationRepository.findByTrainer_Id(trainer.getId(), pageable)
            .map(NotificationResponseDto::fromEntity);
      }
      default -> throw new UserNotFoundException();
    }
  }

  // 매 시간마다 알림을 전송
  @Scheduled(cron = "0 0 * * * *")
  public void schedulePtSessions() {
    LocalDateTime startAt = LocalDateTime.now().plusHours(1)
        .truncatedTo(ChronoUnit.HOURS); // 14:00:00
    List<ScheduleEntity> schedules = scheduleRepository.findByDatesWithDetails(startAt, startAt);

    log.info("{}개의 일정 알림을 전송합니다.", schedules.size());

    schedules.forEach(schedule -> {
      NotificationEntity notification = saveNotification(
          NotificationType.ONE_HOUR_BEFORE_PT_SESSION,
          schedule.getTrainer(),
          schedule.getPtContract().getTrainee(),
          schedule.getStartAt()
      );
      sendNotification(notification);
    });
  }

  private TrainerEntity getTrainer() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return trainerRepository.findByEmail(auth.getName())
        .orElseThrow(UserNotFoundException::new);
  }

  private TraineeEntity getTrainee() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return traineeRepository.findByEmail(auth.getName())
        .orElseThrow(UserNotFoundException::new);
  }

  private UserRoleType getMyRole() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRAINER"))) {
      return UserRoleType.TRAINER;
    } else {
      return UserRoleType.TRAINEE;
    }
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
    if (notificationType == NotificationType.ONE_HOUR_BEFORE_PT_SESSION) {
      message = NotificationMessageGeneratorUtil.oneHourBeforePtSession(trainer.getName(),
          trainee.getName(), startAt);
    }
    NotificationEntity notification = NotificationEntity.of(
        notificationType, true, true,
        trainer, trainee, message,
        startAt.toLocalDate()
    );
    NotificationEntity saved = notificationRepository.save(notification);

    // 알림에 사용되는 토큰들이 Eager로 로드되어 있어야 Scheduler가 실행 가능함.
    // 그렇지 않으면 Hibernate session이 종료되어 LazyInitializationException이 발생
    return notificationRepository.findByIdWithToken(saved.getId())
        .orElseThrow();
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
