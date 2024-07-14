package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.WorkoutImageRequestDto;
import com.project.trainingdiary.dto.request.WorkoutSessionCreateRequestDto;
import com.project.trainingdiary.dto.response.CustomResponse;
import com.project.trainingdiary.dto.response.WorkoutImageResponseDto;
import com.project.trainingdiary.dto.response.WorkoutSessionListResponseDto;
import com.project.trainingdiary.dto.response.WorkoutSessionResponseDto;
import com.project.trainingdiary.service.WorkoutSessionService;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// TODO
//  1. api 명세 보고 수정
//  2. path 에 위치한 id를 dto(body)로 받기
@RestController
@RequiredArgsConstructor
@RequestMapping("api/trainees")
public class WorkoutSessionController {

  private final WorkoutSessionService workoutSessionService;

  @PostMapping("/workout-sessions")
  public CustomResponse<?> createWorkoutSession(
      @RequestBody WorkoutSessionCreateRequestDto dto
  ) {
    workoutSessionService.createWorkoutSession(dto);
    return CustomResponse.success();
  }

  @GetMapping("/{id}/workout-sessions")
  public CustomResponse<?> getWorkoutSessions(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    Page<WorkoutSessionListResponseDto> responsePage = workoutSessionService
        .getWorkoutSessions(id, pageable);
    return CustomResponse.success(responsePage);
  }

  @GetMapping("/{traineeId}/workout-sessions/{sessionId}")
  public CustomResponse<?> getWorkoutSessionDetails(
      @PathVariable Long traineeId,
      @PathVariable Long sessionId
  ) {
    WorkoutSessionResponseDto responseDto = workoutSessionService
        .getWorkoutSessionDetails(traineeId, sessionId);
    return CustomResponse.success(responseDto);
  }

  @PutMapping("/workout-sessions/photos")
  public CustomResponse<?> uploadWorkoutImage(
      @RequestPart("sessionId") Long sessionId,
      @RequestPart("images") List<MultipartFile> images
  ) throws IOException {
    WorkoutImageRequestDto dto = WorkoutImageRequestDto.builder()
        .sessionId(sessionId).images(images).build();
    WorkoutImageResponseDto imageResponseDto = workoutSessionService.uploadWorkoutImage(dto);
    return CustomResponse.success(imageResponseDto);
  }

}
