package com.project.trainingdiary.dto.request;

import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.model.ScheduleStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OpenScheduleRequestDto {

  private List<ScheduleDateTimes> dateTimes;

  public List<ScheduleEntity> toEntities() {

    List<ScheduleEntity> list = new ArrayList<>();

    for (ScheduleDateTimes dateTime : dateTimes) {
      LocalDate startDate = dateTime.getStartDate();
      List<LocalTime> times = dateTime.getStartTimes();

      for (LocalTime startTime : times) {
        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
        LocalDateTime endDateTime = startDateTime.plusHours(1);

        list.add(ScheduleEntity.builder()
            .startDate(startDate)
            .endDate(endDateTime.toLocalDate())
            .startTime(startTime)
            .endTime(endDateTime.toLocalTime())
            .scheduleStatus(ScheduleStatus.OPEN)
            .build()
        );
      }
    }

    return list;
  }
}

@Getter
class ScheduleDateTimes {

  private LocalDate startDate;
  private List<LocalTime> startTimes;
}