package com.project.trainingdiary.repository;

import com.project.trainingdiary.dto.response.ScheduleResponseDto;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepositoryCustom {

  List<ScheduleResponseDto> getScheduleList(LocalDateTime startDateTime, LocalDateTime endDateTime);
}
