package com.project.trainingdiary.dto.response.schedule;

import com.project.trainingdiary.model.ScheduleResponseDetail;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ScheduleResponseDto {

  private LocalDate startDate;
  private boolean existReserved;
  private List<ScheduleResponseDetail> details;
}
