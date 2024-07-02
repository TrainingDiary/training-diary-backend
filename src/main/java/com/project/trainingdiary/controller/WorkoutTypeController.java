package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.WorkoutTypeRequestDto;
import com.project.trainingdiary.dto.response.CommonResponse;
import com.project.trainingdiary.dto.response.WorkoutTypeResponseDto;
import com.project.trainingdiary.service.WorkoutTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/trainers/workout-types")
public class WorkoutTypeController {

  private final WorkoutTypeService workoutTypeService;

  @PostMapping
  public CommonResponse<?> createWorkoutType(
      @Validated @RequestBody WorkoutTypeRequestDto dto
  ) {
    WorkoutTypeResponseDto responseDto = workoutTypeService.createWorkoutType(dto);
    return CommonResponse.created(responseDto);
  }

}
