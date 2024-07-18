package com.project.trainingdiary.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class DietImageResponseDto {

  private List<String> originalUrl;
  private List<String> thumbnailUrl;
  private String content;
}