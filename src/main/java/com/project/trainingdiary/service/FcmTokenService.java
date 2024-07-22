package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.notification.RegisterFcmTokenRequestDto;
import com.project.trainingdiary.entity.FcmTokenEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.user.UserNotFoundException;
import com.project.trainingdiary.model.type.UserRoleType;
import com.project.trainingdiary.repository.FcmTokenRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

  private final FcmTokenRepository fcmTokenRepository;
  private final TrainerRepository trainerRepository;
  private final TraineeRepository traineeRepository;

  public void registerFcmToken(RegisterFcmTokenRequestDto dto) {
    String email = getMyEmail();
    FcmTokenEntity fcmToken;

    switch (getMyRole()) {
      case TRAINER -> {
        TrainerEntity trainer = trainerRepository.findByEmail(email)
            .orElseThrow(UserNotFoundException::new);
        fcmToken = fcmTokenRepository.findByTrainerId(trainer.getId())
            .orElse(FcmTokenEntity.of(dto.getToken(), trainer, null));
        fcmToken.setToken(dto.getToken());
      }
      case TRAINEE -> {
        TraineeEntity trainee = traineeRepository.findByEmail(email)
            .orElseThrow(UserNotFoundException::new);
        fcmToken = fcmTokenRepository.findByTraineeId(trainee.getId())
            .orElse(FcmTokenEntity.of(dto.getToken(), null, trainee));
        fcmToken.setToken(dto.getToken());
      }
      default -> throw new UserNotFoundException();
    }

    fcmTokenRepository.save(fcmToken);
  }

  private String getMyEmail() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
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
