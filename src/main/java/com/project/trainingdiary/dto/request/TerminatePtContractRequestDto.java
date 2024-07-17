package com.project.trainingdiary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TerminatePtContractRequestDto {

  @NotNull(message = "PT 계약 id를 입력해주세요")
  @Schema(example = "1")
  private Long ptContractId;
}
