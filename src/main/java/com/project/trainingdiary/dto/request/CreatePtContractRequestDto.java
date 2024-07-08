package com.project.trainingdiary.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePtContractRequestDto {

  @NotNull(message = "traineeEmail를 입력하세요")
  private String traineeEmail;

  @PositiveOrZero(message = "sessionCount는 0 이상이어야 합니다")
  private int sessionCount;
}
