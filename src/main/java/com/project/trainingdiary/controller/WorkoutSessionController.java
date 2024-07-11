package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.WorkoutSessionCreateRequestDto;
import com.project.trainingdiary.dto.response.CommonResponse;
import com.project.trainingdiary.dto.response.WorkoutSessionListResponseDto;
import com.project.trainingdiary.dto.response.WorkoutSessionResponseDto;
import com.project.trainingdiary.service.WorkoutSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/trainees")
public class WorkoutSessionController {

  private final WorkoutSessionService workoutSessionService;

  @PostMapping("/workout-sessions")
  public CommonResponse<?> createWorkoutSession(
      @RequestBody WorkoutSessionCreateRequestDto dto
  ) {
    workoutSessionService.createWorkoutSession(dto);
    return CommonResponse.created();
  }

  @GetMapping("/{id}/workout-sessions")
  public CommonResponse<?> getWorkoutSessions(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    Page<WorkoutSessionListResponseDto> responsePage = workoutSessionService
        .getWorkoutSessions(id, pageable);
    return CommonResponse.success(responsePage);
  }

  @GetMapping("/{traineeId}/workout-sessions/{sessionId}")
  public CommonResponse<?> getWorkoutSessionDetails(
      @PathVariable Long traineeId,
      @PathVariable Long sessionId
  ) {
    WorkoutSessionResponseDto responseDto = workoutSessionService
        .getWorkoutSessionDetails(traineeId, sessionId);
    return CommonResponse.success(responseDto);
  }

}
