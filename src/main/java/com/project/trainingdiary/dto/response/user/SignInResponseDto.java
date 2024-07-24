package com.project.trainingdiary.dto.response.user;

import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.model.type.UserRoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignInResponseDto {

  private Long id;
  private String email;
  private UserRoleType role;
  private boolean unreadNotification;

  public static SignInResponseDto fromEntity(TrainerEntity trainer) {
    return SignInResponseDto.builder()
        .id(trainer.getId())
        .email(trainer.getEmail())
        .role(UserRoleType.TRAINER)
        .unreadNotification(trainer.isUnreadNotification())
        .build();
  }

  public static SignInResponseDto fromEntity(TraineeEntity trainee) {
    return SignInResponseDto.builder()
        .id(trainee.getId())
        .email(trainee.getEmail())
        .role(UserRoleType.TRAINEE)
        .unreadNotification(trainee.isUnreadNotification())
        .build();
  }
}
