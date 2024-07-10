package com.project.trainingdiary.dto.response;

import static com.project.trainingdiary.model.WorkoutMediaType.IMAGE;
import static com.project.trainingdiary.model.WorkoutMediaType.VIDEO;

import com.project.trainingdiary.dto.request.WorkoutDto;
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

  private Long id;
  private LocalDate sessionDate;
  private int sessionNumber;
  private String specialNote;
  private List<WorkoutDto> workouts;
  private List<String> photoUrls;
  private List<String> videoUrls;

  public static WorkoutSessionResponseDto fromEntity(WorkoutSessionEntity entity) {

    List<WorkoutDto> workoutDtos = entity.getWorkouts().stream()
        .map(WorkoutDto::fromEntity).toList();

    List<String> photoUrls = entity.getWorkoutMedia().stream()
        .filter(media -> media.getMediaType() == IMAGE)
        .map(WorkoutMediaEntity::getMediaUrl)
        .toList();

    List<String> videoUrls = entity.getWorkoutMedia().stream()
        .filter(media -> media.getMediaType() == VIDEO)
        .map(WorkoutMediaEntity::getMediaUrl)
        .toList();

    return WorkoutSessionResponseDto.builder()
        .id(entity.getId())
        .sessionDate(entity.getSessionDate())
        .sessionNumber(entity.getSessionNumber())
        .specialNote(entity.getSpecialNote())
        .workouts(workoutDtos)
        .photoUrls(photoUrls.isEmpty() ? new ArrayList<>() : photoUrls)
        .videoUrls(videoUrls.isEmpty() ? new ArrayList<>() : videoUrls)
        .build();

  }

}
