package com.project.trainingdiary.dto.request.ptcontract;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddPtContractSessionRequestDto {

  @NotNull(message = "트레이니 id를 입력해주세요")
  private Long traineeId;

  @NotNull(message = "추가횟수를 입력해주세요")
  private Integer addition;
}
