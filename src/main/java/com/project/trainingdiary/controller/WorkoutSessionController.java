package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.WorkoutImageRequestDto;
import com.project.trainingdiary.dto.request.WorkoutSessionCreateRequestDto;
import com.project.trainingdiary.dto.request.WorkoutSessionUpdateRequestDto;
import com.project.trainingdiary.dto.request.WorkoutVideoRequestDto;
import com.project.trainingdiary.dto.response.WorkoutImageResponseDto;
import com.project.trainingdiary.dto.response.WorkoutSessionListResponseDto;
import com.project.trainingdiary.dto.response.WorkoutSessionResponseDto;
import com.project.trainingdiary.dto.response.WorkoutVideoResponseDto;
import com.project.trainingdiary.service.WorkoutSessionService;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("api/workout-sessions")
public class WorkoutSessionController {

  private final WorkoutSessionService workoutSessionService;

  @PostMapping
  public ResponseEntity<WorkoutSessionResponseDto> createWorkoutSession(
      @RequestBody WorkoutSessionCreateRequestDto dto
  ) {
    return ResponseEntity.ok(workoutSessionService.createWorkoutSession(dto));
  }

  @PutMapping
  public ResponseEntity<WorkoutSessionResponseDto> updateWorkoutSession(
      @RequestBody WorkoutSessionUpdateRequestDto dto
  ) {
    return ResponseEntity.ok(workoutSessionService.updateWorkoutSession(dto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteWorkoutSession(
      @PathVariable Long id
  ) {
    workoutSessionService.deleteWorkoutSession(id);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/trainees/{id}")
  public ResponseEntity<Page<WorkoutSessionListResponseDto>> getWorkoutSessions(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(workoutSessionService.getWorkoutSessions(id, pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<WorkoutSessionResponseDto> getWorkoutSessionDetails(
      @PathVariable Long id
  ) {
    return ResponseEntity.ok(workoutSessionService.getWorkoutSessionDetails(id));
  }

  @PutMapping("/photos")
  public ResponseEntity<WorkoutImageResponseDto> uploadWorkoutImage(
      @RequestPart("sessionId") Long sessionId,
      @RequestPart("images") List<MultipartFile> images
  ) throws IOException {
    WorkoutImageRequestDto dto = WorkoutImageRequestDto.builder()
        .sessionId(sessionId).images(images).build();
    return ResponseEntity.ok(workoutSessionService.uploadWorkoutImage(dto));
  }

  @PutMapping("/videos")
  public ResponseEntity<WorkoutVideoResponseDto> uploadWorkoutVideo(
      @RequestPart("sessionId") Long sessionId,
      @RequestPart("video") MultipartFile video
  ) throws IOException {
    WorkoutVideoRequestDto dto = WorkoutVideoRequestDto.builder()
        .sessionId(sessionId).video(video).build();
    return ResponseEntity.ok(workoutSessionService.uploadWorkoutVideo(dto));
  }

}
