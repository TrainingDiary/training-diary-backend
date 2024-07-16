package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.WorkoutTypeCreateRequestDto;
import com.project.trainingdiary.dto.request.WorkoutTypeUpdateRequestDto;
import com.project.trainingdiary.dto.response.WorkoutTypeResponseDto;
import com.project.trainingdiary.service.WorkoutTypeService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("api/workout-types")
public class WorkoutTypeController {

  private final WorkoutTypeService workoutTypeService;

  @PreAuthorize("hasRole('TRAINER')")
  @PostMapping
  public ResponseEntity<WorkoutTypeResponseDto> createWorkoutType(
      @Valid @RequestBody WorkoutTypeCreateRequestDto dto
  ) {
    WorkoutTypeResponseDto responseDto = workoutTypeService.createWorkoutType(dto);
    return ResponseEntity.ok(responseDto);
  }

  @PreAuthorize("hasRole('TRAINER')")
  @PutMapping
  public ResponseEntity<WorkoutTypeResponseDto> updateWorkoutType(
      @Valid @RequestBody WorkoutTypeUpdateRequestDto dto
  ) {
    WorkoutTypeResponseDto responseDto = workoutTypeService.updateWorkoutType(dto);
    return ResponseEntity.ok(responseDto);
  }

  @PreAuthorize("hasRole('TRAINER')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteWorkoutType(@PathVariable Long id) {
    workoutTypeService.deleteWorkoutType(id);
    return ResponseEntity.ok().build();
  }

  @PreAuthorize("hasRole('TRAINER')")
  @GetMapping
  public ResponseEntity<Page<WorkoutTypeResponseDto>> getWorkoutTypes(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    Page<WorkoutTypeResponseDto> responsePage = workoutTypeService.getWorkoutTypes(pageable);
    return ResponseEntity.ok(responsePage);
  }

  @PreAuthorize("hasRole('TRAINER')")
  @GetMapping("/{id}")
  public ResponseEntity<WorkoutTypeResponseDto> getWorkoutTypeDetails(@PathVariable Long id) {
    WorkoutTypeResponseDto responseDto = workoutTypeService.getWorkoutTypeDetails(id);
    return ResponseEntity.ok(responseDto);
  }

}
