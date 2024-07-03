package com.project.trainingdiary.service.impl;

import com.project.trainingdiary.dto.request.WorkoutTypeCreateRequestDto;
import com.project.trainingdiary.dto.response.WorkoutTypeResponseDto;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.entity.WorkoutTypeEntity;
import com.project.trainingdiary.exception.TrainerNotFoundException;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.WorkoutTypeRepository;
import com.project.trainingdiary.service.WorkoutTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkoutTypeServiceImpl implements WorkoutTypeService {

  private final WorkoutTypeRepository workoutTypeRepository;
  private final TrainerRepository trainerRepository;

  /*
   * 트레이너의 운동 타입 등록
   */
  @Transactional
  public WorkoutTypeResponseDto createWorkoutType(WorkoutTypeCreateRequestDto dto) {

    TrainerEntity trainerEntity = trainerRepository.findById(dto.getTrainerId()).orElseThrow(
        () -> new TrainerNotFoundException("트레이너를 찾을 수 없습니다. ID: " + dto.getTrainerId()));

    WorkoutTypeEntity workoutTypeEntity = workoutTypeRepository.save(
        WorkoutTypeCreateRequestDto.toEntity(dto, trainerEntity));

    return WorkoutTypeResponseDto.of(workoutTypeEntity);

  }

  /**
   * 트레이너의 운동 타입 목록 조회
   */
  @Override
  public Page<WorkoutTypeResponseDto> getWorkoutTypes(Long id, Pageable pageable) {
    Page<WorkoutTypeEntity> workoutTypePage = workoutTypeRepository.findByTrainer_Id(id, pageable);
    return workoutTypePage.map(WorkoutTypeResponseDto::of);
  }

}
