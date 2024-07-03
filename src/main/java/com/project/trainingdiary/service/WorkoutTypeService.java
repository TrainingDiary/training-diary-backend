package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.WorkoutTypeCreateRequestDto;
import com.project.trainingdiary.dto.response.WorkoutTypeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkoutTypeService {

  WorkoutTypeResponseDto createWorkoutType(WorkoutTypeCreateRequestDto dto);

  Page<WorkoutTypeResponseDto> getWorkoutTypes(Long id, Pageable pageable);

}
