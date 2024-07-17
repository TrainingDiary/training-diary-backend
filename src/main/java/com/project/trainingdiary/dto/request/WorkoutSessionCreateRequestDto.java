package com.project.trainingdiary.dto.request;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutSessionCreateRequestDto {

  private Long traineeId;

  private LocalDate sessionDate;
  private int sessionNumber;
  private String specialNote;

  private List<WorkoutDto> workouts;

}
