package com.project.trainingdiary.dto.response.workout.session;

import com.project.trainingdiary.entity.WorkoutMediaEntity;
import com.project.trainingdiary.util.ConvertCloudFrontUrlUtil;
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
public class WorkoutImageResponseDto {

  private Long sessionId;
  private List<String> originalUrls;
  private List<String> thumbnailUrls;

  public static WorkoutImageResponseDto fromEntity(
      List<WorkoutMediaEntity> workoutMediaList,
      Long sessionId
  ) {

    return WorkoutImageResponseDto.builder()
        .sessionId(sessionId)
        .originalUrls(workoutMediaList.stream().map(WorkoutMediaEntity::getOriginalUrl)
            .map(ConvertCloudFrontUrlUtil::convertToCloudFrontUrl).toList())
        .thumbnailUrls(workoutMediaList.stream().map(WorkoutMediaEntity::getThumbnailUrl)
            .map(ConvertCloudFrontUrlUtil::convertToCloudFrontUrl).toList())
        .build();

  }

}
