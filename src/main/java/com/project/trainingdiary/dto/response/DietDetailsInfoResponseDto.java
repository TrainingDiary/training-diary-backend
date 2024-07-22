package com.project.trainingdiary.dto.response;

import com.project.trainingdiary.entity.DietEntity;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DietDetailsInfoResponseDto {

  private Long id;
  private String imageUrl;
  private String content;
  private LocalDateTime createdDate;

  public static DietDetailsInfoResponseDto of(DietEntity diet) {
    return DietDetailsInfoResponseDto.builder()
        .id(diet.getId())
        .imageUrl(diet.getOriginalUrl())
        .content(diet.getContent())
        .createdDate(diet.getCreatedAt())
        .build();
  }
}
