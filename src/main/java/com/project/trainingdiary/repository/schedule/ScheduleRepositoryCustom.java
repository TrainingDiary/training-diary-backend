package com.project.trainingdiary.repository.schedule;

import com.project.trainingdiary.dto.response.schedule.ScheduleResponseDto;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepositoryCustom {

  List<ScheduleResponseDto> getScheduleListByTrainer(
      long trainerId,
      LocalDateTime startDateTime,
      LocalDateTime endDateTime
  );

  List<ScheduleResponseDto> getScheduleListByTrainee(
      Long trainerId,
      long traineeId,
      LocalDateTime startDateTime,
      LocalDateTime endDateTime
  );
}
