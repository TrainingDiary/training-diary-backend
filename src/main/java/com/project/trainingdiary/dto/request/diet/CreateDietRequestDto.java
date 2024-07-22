package com.project.trainingdiary.dto.request.diet;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class CreateDietRequestDto {

  @Schema(example = "6월 27일 오늘 점심 식단입니다!")
  @NotNull(message = "content을 입력하세요")
  private String content;

  @Schema(example = "image1.png")
  @NotNull(message = "image를 업로드하세요.")
  private MultipartFile image;
}
