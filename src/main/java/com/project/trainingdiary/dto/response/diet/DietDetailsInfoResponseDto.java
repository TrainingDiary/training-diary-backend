package com.project.trainingdiary.dto.response.diet;

import com.project.trainingdiary.entity.DietEntity;
import com.project.trainingdiary.entity.TrainerCommentEntity;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DietDetailsInfoResponseDto {

  private Long id;
  private String imageUrl;
  private String content;
  private List<TrainerCommentEntity> trainerComments;
  private LocalDateTime createdDate;

  public static DietDetailsInfoResponseDto of(DietEntity diet,
      List<TrainerCommentEntity> trainerComment) {
    return DietDetailsInfoResponseDto.builder()
        .id(diet.getId())
        .imageUrl(diet.getOriginalUrl())
        .content(diet.getContent())
        .trainerComments(trainerComment)
        .createdDate(diet.getCreatedAt())
        .build();
  }
}
