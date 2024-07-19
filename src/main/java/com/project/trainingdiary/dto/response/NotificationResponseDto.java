package com.project.trainingdiary.dto.response;

import com.project.trainingdiary.entity.NotificationEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationResponseDto {

  private Long id;
  private int content;
  private String trainerName;
  private String traineeName;

  public static NotificationResponseDto fromEntity(NotificationEntity entity) {
    NotificationResponseDto dto = new NotificationResponseDto();
    dto.setId(entity.getId());
    dto.setContent(entity.getContent());
    dto.setTrainerName(entity.getTrainer().getName());
    dto.setTraineeName(entity.getTrainee().getName());
    return dto;
  }
}
