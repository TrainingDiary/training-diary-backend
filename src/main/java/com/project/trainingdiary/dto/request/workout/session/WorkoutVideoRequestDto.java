package com.project.trainingdiary.dto.request.workout.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class WorkoutVideoRequestDto {

  @Positive
  @NotNull(message = "운동 일지의 id 값을 입력해주세요.")
  @Schema(description = "운동 일지의 id", example = "1")
  private Long sessionId;

  @Schema(description = "운동 영상", example = "[\"example1.mp4\"")
  private MultipartFile video;

}
