package com.project.trainingdiary.dto.response.diet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class DietImageResponseDto {

  private Long dietId;
  private String thumbnailUrl;
}