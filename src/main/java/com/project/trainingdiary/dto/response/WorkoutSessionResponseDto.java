package com.project.trainingdiary.dto.response;

import static com.project.trainingdiary.model.WorkoutMediaType.IMAGE;
import static com.project.trainingdiary.model.WorkoutMediaType.VIDEO;

import com.project.trainingdiary.entity.WorkoutMediaEntity;
import com.project.trainingdiary.entity.WorkoutSessionEntity;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
  private List<String> videoUrls;

  public static WorkoutSessionResponseDto fromEntity(WorkoutSessionEntity entity) {

    List<WorkoutResponseDto> workoutDtos = entity.getWorkouts().stream()
        .map(WorkoutResponseDto::fromEntity).toList();

    List<WorkoutMediaEntity> workoutMedia = entity.getWorkoutMedia();
    List<String> photoUrls = new ArrayList<>();
    List<String> videoUrls = new ArrayList<>();

    if (workoutMedia != null && !workoutMedia.isEmpty()) {
      photoUrls = workoutMedia.stream()
          .filter(media -> media.getMediaType().equals(IMAGE))
          .map(WorkoutMediaEntity::getOriginalUrl)
          .toList();

      videoUrls = workoutMedia.stream()
          .filter(media -> media.getMediaType().equals(VIDEO))
          .map(WorkoutMediaEntity::getOriginalUrl)
          .toList();
    }

    return WorkoutSessionResponseDto.builder()
        .sessionId(entity.getId())
        .sessionDate(entity.getSessionDate())
        .sessionNumber(entity.getSessionNumber())
        .specialNote(entity.getSpecialNote())
        .workouts(workoutDtos)
        .photoUrls(photoUrls.isEmpty() ? new ArrayList<>() : photoUrls)
        .videoUrls(videoUrls.isEmpty() ? new ArrayList<>() : videoUrls)
        .build();

  }

}
