package com.project.trainingdiary.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PtContractResponseDto {

  private Long id;
  private LocalDateTime totalSessionUpdatedAt;
  private int totalSession;
  private int usedSession;
  private Long trainerId;
  private Long traineeId;
  private LocalDateTime createdAt;
}
