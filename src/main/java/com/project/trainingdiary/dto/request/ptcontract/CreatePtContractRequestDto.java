package com.project.trainingdiary.dto.request.ptcontract;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePtContractRequestDto {

  @NotNull(message = "traineeEmail을 입력하세요")
  @Schema(example = "hello@example.com")
  private String traineeEmail;
}
