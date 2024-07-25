package com.project.trainingdiary.dto.response.ptcontract;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PtContractResponseDto {

  private Long ptContractId;
  private LocalDate totalSessionUpdatedAt;
  private int remainingSession;
  private Long trainerId;
  private String trainerName;
  private Long traineeId;
  private String traineeName;
}
