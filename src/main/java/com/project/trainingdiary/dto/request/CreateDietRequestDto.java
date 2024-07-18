package com.project.trainingdiary.dto.request;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class CreateDietRequestDto {

  private String content;
  List<MultipartFile> images;
}
