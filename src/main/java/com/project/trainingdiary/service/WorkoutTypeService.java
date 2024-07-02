package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.WorkoutTypeRequestDto;
import com.project.trainingdiary.dto.response.WorkoutTypeResponseDto;

public interface WorkoutTypeService {

  WorkoutTypeResponseDto createWorkoutType(WorkoutTypeRequestDto dto);

}
