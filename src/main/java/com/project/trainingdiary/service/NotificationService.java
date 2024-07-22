package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.response.notification.NotificationResponseDto;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.user.UserNotFoundException;
import com.project.trainingdiary.model.type.UserRoleType;
import com.project.trainingdiary.repository.NotificationRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final TrainerRepository trainerRepository;
  private final TraineeRepository traineeRepository;

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
}
