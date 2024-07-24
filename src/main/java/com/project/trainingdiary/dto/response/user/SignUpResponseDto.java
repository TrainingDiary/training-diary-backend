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
public class SignUpResponseDto {

  private Long id;
  private String email;
  private UserRoleType role;
  private boolean unReadNotification;

  public static SignUpResponseDto fromEntity(TrainerEntity trainer) {
    return SignUpResponseDto.builder()
        .id(trainer.getId())
        .email(trainer.getEmail())
        .role(UserRoleType.TRAINER)
        .unReadNotification(trainer.isUnreadNotification())
        .build();
  }

  public static SignUpResponseDto fromEntity(TraineeEntity trainee) {
    return SignUpResponseDto.builder()
        .id(trainee.getId())
        .email(trainee.getEmail())
        .role(UserRoleType.TRAINEE)
        .unReadNotification(trainee.isUnreadNotification())
        .build();
  }
}
