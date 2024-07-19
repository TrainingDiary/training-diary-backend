package com.project.trainingdiary.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PtContractResponseDto {

  private Long ptContractId;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "Asia/Seoul")
  private ZonedDateTime totalSessionUpdatedAt;
  private int remainingSession;
  private Long trainerId;
  private String trainerName;
  private Long traineeId;
  private String traineeName;
}
