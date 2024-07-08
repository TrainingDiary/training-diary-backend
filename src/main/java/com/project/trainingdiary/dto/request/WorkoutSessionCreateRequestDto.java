package com.project.trainingdiary.dto.request;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutSessionCreateRequestDto {

  private LocalDate sessionDate;
  private int sessionNumber;
  private String  specialNote;

  private List<WorkoutCreateRequestDto> workouts;

}
