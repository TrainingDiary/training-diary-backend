package com.project.trainingdiary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutImageRequestDto {

  @Positive @NotNull(message = "운동 일지의 id 값을 입력해주세요.")
  @Schema(description = "운동 일지의 id", example = "1")
  private Long sessionId;

  @Schema(description = "자세 사진",
      example = "[\"example1.png\", \"example2.jpeg\"], ...")
  private List<MultipartFile> images;

}
