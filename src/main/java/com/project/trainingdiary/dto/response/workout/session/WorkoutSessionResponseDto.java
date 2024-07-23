package com.project.trainingdiary.dto.response.workout.session;

import static com.project.trainingdiary.model.type.WorkoutMediaType.IMAGE;
import static com.project.trainingdiary.model.type.WorkoutMediaType.VIDEO;

import com.project.trainingdiary.entity.WorkoutMediaEntity;
import com.project.trainingdiary.entity.WorkoutSessionEntity;
import com.project.trainingdiary.util.ConvertCloudFrontUrlUtil;
import java.time.LocalDate;
import java.util.ArrayList;
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
public class WorkoutSessionResponseDto {

  private Long sessionId;
  private LocalDate sessionDate;
  private int sessionNumber;
  private String specialNote;
  private List<WorkoutResponseDto> workouts;
  private List<String> photoUrls;
  private List<String> thumbnailUrls;   // 동영상 썸네일
  private List<String> videoUrls;

  public static WorkoutSessionResponseDto fromEntity(WorkoutSessionEntity entity) {

    List<WorkoutResponseDto> workoutDtos = entity.getWorkouts().stream()
        .map(WorkoutResponseDto::fromEntity).toList();

    List<WorkoutMediaEntity> workoutMedia = Optional.ofNullable(entity.getWorkoutMedia())
        .orElse(Collections.emptyList());

    List<String> photoUrls = workoutMedia.stream()
        .filter(media -> media.getMediaType().equals(IMAGE))
        .map(WorkoutMediaEntity::getOriginalUrl)
        .map(ConvertCloudFrontUrlUtil::convertToCloudFrontUrl)
        .toList();

    List<String> thumbnailUrls = workoutMedia.stream()
        .filter(media -> media.getMediaType().equals(VIDEO))
        .map(WorkoutMediaEntity::getThumbnailUrl)
        .map(ConvertCloudFrontUrlUtil::convertToCloudFrontUrl)
        .toList();

    List<String> videoUrls = workoutMedia.stream()
        .filter(media -> media.getMediaType().equals(VIDEO))
        .map(WorkoutMediaEntity::getOriginalUrl)
        .map(ConvertCloudFrontUrlUtil::convertToCloudFrontUrl)
        .toList();

    return WorkoutSessionResponseDto.builder()
        .sessionId(entity.getId())
        .sessionDate(entity.getSessionDate())
        .sessionNumber(entity.getSessionNumber())
        .specialNote(entity.getSpecialNote())
        .workouts(workoutDtos)
        .photoUrls(photoUrls.isEmpty() ? new ArrayList<>() : photoUrls)
        .thumbnailUrls(thumbnailUrls.isEmpty() ? new ArrayList<>() : thumbnailUrls)
        .videoUrls(videoUrls.isEmpty() ? new ArrayList<>() : videoUrls)
        .build();

  }

}
