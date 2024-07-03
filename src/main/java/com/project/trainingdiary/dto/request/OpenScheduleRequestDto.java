package com.project.trainingdiary.dto.request;

import com.project.trainingdiary.entity.ScheduleEntity;
import com.project.trainingdiary.model.ScheduleStatus;
import jakarta.validation.constraints.NotNull;
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

  @NotNull(message = "dateTimes를 입력해주세요")
  public List<ScheduleDateTimes> dateTimes;

  public List<ScheduleEntity> toEntities() {

    List<ScheduleEntity> list = new ArrayList<>();

    for (ScheduleDateTimes dateTime : dateTimes) {
      LocalDate startDate = dateTime.getStartDate();
      List<LocalTime> times = dateTime.getStartTimes();

      for (LocalTime startTime : times) {
        LocalDateTime startAt = LocalDateTime.of(startDate, startTime);
        LocalDateTime endAt = startAt.plusHours(1);

        list.add(ScheduleEntity.builder()
            .startAt(startAt)
            .endAt(endAt)
            .scheduleStatus(ScheduleStatus.OPEN)
            .build()
        );
      }
    }

    return list;
  }
}