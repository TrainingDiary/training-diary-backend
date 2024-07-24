package com.project.trainingdiary.dto.response.notification;

import com.project.trainingdiary.entity.NotificationEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificationResponseDto {

  private Long notificationId;
  private String note;
  private LocalDate eventDate;
  private LocalDateTime createdAt;

  public static NotificationResponseDto fromEntity(NotificationEntity entity) {
    return NotificationResponseDto.builder()
        .notificationId(entity.getId())
        .note(entity.getBody())
        .eventDate(entity.getEventDate())
        .createdAt(entity.getCreatedAt())
        .build();
  }
}
