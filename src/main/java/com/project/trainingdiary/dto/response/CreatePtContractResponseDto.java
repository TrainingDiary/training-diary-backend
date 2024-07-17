package com.project.trainingdiary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreatePtContractResponseDto {

  @Schema(example = "1")
  private Long ptContractId;
}
