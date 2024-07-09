package com.project.trainingdiary.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TerminatePtContractRequestDto {

  @NotNull(message = "PT 계약 id를 입력해주세요")
  private Long ptContractId;
}
