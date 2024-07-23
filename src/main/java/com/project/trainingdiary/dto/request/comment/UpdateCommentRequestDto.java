package com.project.trainingdiary.dto.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCommentRequestDto {

  @Schema(description = "식단 댓글의 id", example = "1")
  @NotNull(message = "id 값은 null이 없습니다.")
  private Long id;

  @Schema(description = "식단 댓글", example = "좋아요")
  @NotNull(message = "comment 값은 null이 없습니다.")
  private String comment;
}
