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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

  private final TrainerRepository trainerRepository;
  private final TraineeRepository traineeRepository;
  private final FcmTokenRepository fcmTokenEntityRepository;

  @Transactional
  public void registerFcmToken(RegisterFcmTokenRequestDto dto) {
    String email = getMyEmail();

    switch (getMyRole()) {
      case TRAINER -> {
        TrainerEntity trainer = trainerRepository.findByEmail(email)
            .orElseThrow(UserNotFoundException::new);
        FcmTokenEntity token = FcmTokenEntity.builder()
            .token(dto.getToken())
            .build();

        if (trainer.getFcmToken() != null) {
          fcmTokenEntityRepository.delete(trainer.getFcmToken());
        }
        trainer.setFcmToken(token);
        fcmTokenEntityRepository.save(token);
        trainerRepository.save(trainer);
      }
      case TRAINEE -> {
        TraineeEntity trainee = traineeRepository.findByEmail(email)
            .orElseThrow(UserNotFoundException::new);
        FcmTokenEntity token = FcmTokenEntity.builder()
            .token(dto.getToken())
            .build();
        trainee.setFcmToken(token);
        fcmTokenEntityRepository.save(token);
        traineeRepository.save(trainee);
      }
      default -> throw new UserNotFoundException();
    }
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
