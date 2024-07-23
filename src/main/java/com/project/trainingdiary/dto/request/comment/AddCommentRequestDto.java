package com.project.trainingdiary.dto.request.comment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCommentRequestDto {

  private Long dietId;
  private String comment;
}
