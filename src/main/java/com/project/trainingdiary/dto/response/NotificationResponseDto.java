package com.project.trainingdiary.dto.response;

import com.project.trainingdiary.entity.NotificationEntity;
import com.project.trainingdiary.model.NotificationType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationResponseDto {

  private Long id;
  private NotificationType notificationType;
  private String note;
  private String trainerName;
  private String traineeName;
  private LocalDateTime createdAt;

  public static NotificationResponseDto fromEntity(NotificationEntity entity) {
    NotificationResponseDto dto = new NotificationResponseDto();
    dto.setId(entity.getId());
    dto.setNotificationType(entity.getNotificationType());
    dto.setNote(entity.getNote());
    dto.setTrainerName(entity.getTrainer().getName());
    dto.setTraineeName(entity.getTrainee().getName());
    dto.setCreatedAt(entity.getCreatedAt());
    return dto;
  }
}
