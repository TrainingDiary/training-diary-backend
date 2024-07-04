package com.project.trainingdiary.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.model.ScheduleStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ScheduleResponseDto {

  private Long id;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
  private LocalTime startTime;
  private LocalDate startDate;
  private ScheduleStatus status;

  public static ScheduleResponseDto of(ScheduleEntity entity) {
    return ScheduleResponseDto.builder()
        .id(entity.getId())
        .startDate(entity.getStartAt().toLocalDate())
        .startTime(entity.getStartAt().toLocalTime())
        .status(entity.getScheduleStatus())
        .build();
  }
}
