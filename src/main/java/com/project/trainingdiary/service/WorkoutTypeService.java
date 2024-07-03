package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.WorkoutTypeCreateRequestDto;
import com.project.trainingdiary.dto.request.WorkoutTypeUpdateRequestDto;
import com.project.trainingdiary.dto.response.WorkoutTypeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkoutTypeService {

  void createWorkoutType(WorkoutTypeCreateRequestDto dto);

  void updateWorkoutType(Long trainerId, Long workoutTypeId,
      WorkoutTypeUpdateRequestDto dto);

  Page<WorkoutTypeResponseDto> getWorkoutTypes(Long id, Pageable pageable);

}
