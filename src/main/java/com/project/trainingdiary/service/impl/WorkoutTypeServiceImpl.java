package com.project.trainingdiary.service.impl;

import com.project.trainingdiary.dto.request.WorkoutTypeCreateRequestDto;
import com.project.trainingdiary.dto.request.WorkoutTypeUpdateRequestDto;
import com.project.trainingdiary.dto.response.WorkoutTypeResponseDto;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.entity.WorkoutTypeEntity;
import com.project.trainingdiary.exception.TrainerNotFoundException;
import com.project.trainingdiary.exception.WorkoutTypeNotFoundException;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.WorkoutTypeRepository;
import com.project.trainingdiary.service.WorkoutTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO
//  트레이너 exception 수정되는 경우 서비스 코드도 수정

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutTypeServiceImpl implements WorkoutTypeService {

  private final WorkoutTypeRepository workoutTypeRepository;
  private final TrainerRepository trainerRepository;

  /*
   * 트레이너의 운동 타입 등록
   */
  @Transactional
  public void createWorkoutType(WorkoutTypeCreateRequestDto dto) {

    TrainerEntity trainerEntity = trainerRepository.findById(dto.getTrainerId())
        .orElseThrow(() -> new TrainerNotFoundException(dto.getTrainerId()));

    workoutTypeRepository.save(WorkoutTypeCreateRequestDto.toEntity(dto, trainerEntity));

  }

  /**
   * 트레이너의 운동 타입 수정
   */
  @Override
  public void updateWorkoutType(
      Long trainerId, Long workoutTypeId,
      WorkoutTypeUpdateRequestDto dto
  ) {
    WorkoutTypeEntity entity = workoutTypeRepository.findByTrainer_IdAndId(trainerId, workoutTypeId)
        .orElseThrow(() -> new WorkoutTypeNotFoundException(workoutTypeId));

    workoutTypeRepository.save(WorkoutTypeUpdateRequestDto.updateEntity(dto, entity));
  }

  /**
   * 트레이너의 운동 타입 목록 조회
   */
  @Override
  public Page<WorkoutTypeResponseDto> getWorkoutTypes(Long id, Pageable pageable) {
    Page<WorkoutTypeEntity> page = workoutTypeRepository.findByTrainer_Id(id, pageable);
    return page.map(WorkoutTypeResponseDto::of);
  }

}
