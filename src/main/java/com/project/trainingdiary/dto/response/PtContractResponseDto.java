package com.project.trainingdiary.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PtContractResponseDto {

  private Long ptContractId;
  private LocalDateTime totalSessionUpdatedAt;
  private int totalSession;
  private int usedSession;
  private int remainSession;
  private Long trainerId;
  private String trainerName;
  private Long traineeId;
  private String traineeName;
}
