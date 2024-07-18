package com.project.trainingdiary.dto.response;

import com.project.trainingdiary.entity.WorkoutSessionEntity;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkoutSessionListResponseDto {

  private Long sessionId;
  private LocalDate sessionDate;
  private int sessionNumber;

  public static WorkoutSessionListResponseDto fromEntity(WorkoutSessionEntity entity) {

    return WorkoutSessionListResponseDto.builder()
        .sessionId(entity.getId())
        .sessionDate(entity.getSessionDate())
        .sessionNumber(entity.getSessionNumber())
        .build();

  }

}
