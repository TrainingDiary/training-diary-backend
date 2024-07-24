package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.workout.type.WorkoutTypeCreateRequestDto;
import com.project.trainingdiary.dto.request.workout.type.WorkoutTypeUpdateRequestDto;
import com.project.trainingdiary.dto.response.workout.type.WorkoutTypeResponseDto;
import com.project.trainingdiary.service.WorkoutTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "3 - Workout Type API", description = "운동 종류를 위한 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/workout-types")
public class WorkoutTypeController {

  private final WorkoutTypeService workoutTypeService;

  @Operation(
      summary = "운동 종류 생성",
      description = "트레이너가 트레이니에게 제공할 운동 종류를 생성함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공")
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PostMapping
  public ResponseEntity<WorkoutTypeResponseDto> createWorkoutType(
      @Valid @RequestBody WorkoutTypeCreateRequestDto dto
  ) {
    return ResponseEntity.ok(workoutTypeService.createWorkoutType(dto));
  }

  @Operation(
      summary = "운동 종류 수정",
      description = "트레이너가 운동 종류를 수정함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "운동 종류를 찾을 수 없음", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PutMapping
  public ResponseEntity<WorkoutTypeResponseDto> updateWorkoutType(
      @Valid @RequestBody WorkoutTypeUpdateRequestDto dto
  ) {
    return ResponseEntity.ok(workoutTypeService.updateWorkoutType(dto));
  }

  @Operation(
      summary = "운동 종류 삭제",
      description = "트레이너가 필요 없는 운동 종류를 삭제함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "운동 종류를 찾을 수 없음", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteWorkoutType(
      @PathVariable Long id
  ) {
    workoutTypeService.deleteWorkoutType(id);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "운동 종류 목록 조회",
      description = "트레이너가 트레이니에게 제공하는 운동 종류의 목록을 조회함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공")
  })
  @PreAuthorize("hasRole('TRAINER')")
  @GetMapping
  public ResponseEntity<Page<WorkoutTypeResponseDto>> getWorkoutTypes(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(workoutTypeService.getWorkoutTypes(pageable));
  }

  @Operation(
      summary = "운동 종류 상세 조회",
      description = "트레이너가 트레이니에게 제공하는 운동 종류를 상세 조회함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "운동 종류를 찾을 수 없음", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @GetMapping("/{id}")
  public ResponseEntity<WorkoutTypeResponseDto> getWorkoutTypeDetails(
      @PathVariable Long id
  ) {
    return ResponseEntity.ok(workoutTypeService.getWorkoutTypeDetails(id));
  }

}
