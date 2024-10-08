package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.workout.session.WorkoutImageRequestDto;
import com.project.trainingdiary.dto.request.workout.session.WorkoutSessionCreateRequestDto;
import com.project.trainingdiary.dto.request.workout.session.WorkoutSessionUpdateRequestDto;
import com.project.trainingdiary.dto.request.workout.session.WorkoutVideoRequestDto;
import com.project.trainingdiary.dto.response.workout.session.WorkoutImageResponseDto;
import com.project.trainingdiary.dto.response.workout.session.WorkoutSessionListResponseDto;
import com.project.trainingdiary.dto.response.workout.session.WorkoutSessionResponseDto;
import com.project.trainingdiary.dto.response.workout.session.WorkoutVideoResponseDto;
import com.project.trainingdiary.service.WorkoutSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@Tag(name = "4 - Workout Session API", description = "운동 일지를 위한 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/workout-sessions")
public class WorkoutSessionController {

  private final WorkoutSessionService workoutSessionService;

  @Operation(
      summary = "운동 일지 생성",
      description = "트레이너가 운동 일지를 생성함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "PT 계약이 존재하지 않음", content = @Content),
      @ApiResponse(responseCode = "409", description = "해당 PT 수업에 대한 일지가 이미 존재함", content = @Content),
      @ApiResponse(responseCode = "404", description = "운동 종류를 찾을 수 없음", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PostMapping
  public ResponseEntity<WorkoutSessionResponseDto> createWorkoutSession(
      @RequestBody WorkoutSessionCreateRequestDto dto
  ) {
    return ResponseEntity.ok(workoutSessionService.createWorkoutSession(dto));
  }

  @Operation(
      summary = "운동 일지 수정",
      description = "트레이너가 운동 일지를 수정함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "운동 일지를 찾을 수 없음", content = @Content),
      @ApiResponse(responseCode = "404", description = "운동 종류를 찾을 수 없음", content = @Content),
      @ApiResponse(responseCode = "404", description = "운동 기록을 찾을 수 없음", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PutMapping
  public ResponseEntity<WorkoutSessionResponseDto> updateWorkoutSession(
      @RequestBody WorkoutSessionUpdateRequestDto dto
  ) {
    return ResponseEntity.ok(workoutSessionService.updateWorkoutSession(dto));
  }

  @Operation(
      summary = "운동 일지 삭제",
      description = "트레이너가 운동 일지를 삭제함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "운동 일지를 찾을 수 없음", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteWorkoutSession(
      @PathVariable Long id
  ) {
    workoutSessionService.deleteWorkoutSession(id);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "운동 일지 목록 조회",
      description = "트레이너와 트레이니가 운동 일지의 목록을 조회함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공")
  })
  @PreAuthorize("hasRole('TRAINER') or hasRole('TRAINEE')")
  @GetMapping("/trainees/{id}")
  public ResponseEntity<Page<WorkoutSessionListResponseDto>> getWorkoutSessions(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(workoutSessionService.getWorkoutSessions(id, pageable));
  }

  @Operation(
      summary = "운동 일지 상세 조회",
      description = "트레이너와 트레이니가 운동 일지를 상세 조회함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "운동 일지를 찾을 수 없음", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER') or hasRole('TRAINEE')")
  @GetMapping("/{id}")
  public ResponseEntity<WorkoutSessionResponseDto> getWorkoutSessionDetails(
      @PathVariable Long id
  ) {
    return ResponseEntity.ok(workoutSessionService.getWorkoutSessionDetails(id));
  }

  @Operation(
      summary = "운동 일지 이미지 업로드",
      description = "트레이너가 운동 일지에 자세 사진을 업로드함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "운동 일지를 찾을 수 없음", content = @Content),
      @ApiResponse(responseCode = "413", description = "사진 업로드 개수는 10개까지 가능", content = @Content),
      @ApiResponse(responseCode = "415", description = "파일 타입 확인 필요", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PutMapping(value = "/photos",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<WorkoutImageResponseDto> uploadWorkoutImage(
      @RequestPart("sessionId") Long sessionId,
      @RequestPart("images") List<MultipartFile> images
  ) throws IOException {
    WorkoutImageRequestDto dto = WorkoutImageRequestDto.builder()
        .sessionId(sessionId)
        .images(images)
        .build();
    return ResponseEntity.ok(workoutSessionService.uploadWorkoutImage(dto));
  }

  @Operation(
      summary = "운동 일지 동영상 업로드",
      description = "트레이너가 운동 일지에 동영상을 업로드함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "운동 일지를 찾을 수 없음", content = @Content),
      @ApiResponse(responseCode = "413", description = "동영상 업로드 개수는 5개까지 가능", content = @Content),
      @ApiResponse(responseCode = "415", description = "파일 타입 확인 필요", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PutMapping(value = "/videos",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<WorkoutVideoResponseDto> uploadWorkoutVideo(
      @RequestPart("sessionId") Long sessionId,
      @RequestPart("video") MultipartFile video
  ) throws IOException, InterruptedException {
    WorkoutVideoRequestDto dto = WorkoutVideoRequestDto.builder()
        .sessionId(sessionId)
        .video(video)
        .build();
    return ResponseEntity.ok(workoutSessionService.uploadWorkoutVideo(dto));
  }

}
