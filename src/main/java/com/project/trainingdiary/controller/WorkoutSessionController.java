package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.WorkoutSessionCreateRequestDto;
import com.project.trainingdiary.dto.response.CommonResponse;
import com.project.trainingdiary.service.WorkoutSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/trainees")
public class WorkoutSessionController {

  private final WorkoutSessionService workoutSessionService;

  @PostMapping("workout-sessions")
  public CommonResponse<?> createWorkoutSession(
      @RequestBody WorkoutSessionCreateRequestDto dto
  ) {
    workoutSessionService.createWorkoutSession(dto);
    return CommonResponse.created();
  }

}
