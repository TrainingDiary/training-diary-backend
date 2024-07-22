package com.project.trainingdiary.dto.request.diet;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class CreateDietRequestDto {

  private String content;
  private MultipartFile image;
}
