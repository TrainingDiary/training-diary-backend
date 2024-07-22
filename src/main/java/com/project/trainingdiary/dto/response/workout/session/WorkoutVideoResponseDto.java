package com.project.trainingdiary.dto.response.workout.session;

import com.project.trainingdiary.entity.WorkoutMediaEntity;
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
public class WorkoutVideoResponseDto {

  private Long sessionId;
  private List<String> originalUrls;

  public static WorkoutVideoResponseDto fromEntity(
      List<WorkoutMediaEntity> workoutMediaList,
      Long sessionId
  ) {

    return WorkoutVideoResponseDto.builder()
        .sessionId(sessionId)
        .originalUrls(workoutMediaList.stream().map(WorkoutMediaEntity::getOriginalUrl).toList())
        .build();

  }

}
