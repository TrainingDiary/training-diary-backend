package com.project.trainingdiary.dto.response.workout.session;

import static com.project.trainingdiary.model.type.WorkoutMediaType.IMAGE;

import com.project.trainingdiary.entity.WorkoutMediaEntity;
import com.project.trainingdiary.entity.WorkoutSessionEntity;
import com.project.trainingdiary.util.ConvertCloudFrontUrlUtil;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
  private List<String> thumbnailUrls;

  public static WorkoutSessionListResponseDto fromEntity(WorkoutSessionEntity entity) {

    List<String> imageUrls = Optional.ofNullable(entity.getWorkoutMedia())
        .orElse(Collections.emptyList())
        .stream()
        .filter(media -> media.getMediaType().equals(IMAGE))
        .map(WorkoutMediaEntity::getThumbnailUrl)
        .map(ConvertCloudFrontUrlUtil::convertToCloudFrontUrl)
        .toList();

    return WorkoutSessionListResponseDto.builder()
        .sessionId(entity.getId())
        .sessionDate(entity.getSessionDate())
        .sessionNumber(entity.getSessionNumber())
        .thumbnailUrls(imageUrls)
        .build();

  }

}
