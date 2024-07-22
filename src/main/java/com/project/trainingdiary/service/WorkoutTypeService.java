package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.workout.type.WorkoutTypeCreateRequestDto;
import com.project.trainingdiary.dto.request.workout.type.WorkoutTypeUpdateRequestDto;
import com.project.trainingdiary.dto.response.workout.type.WorkoutTypeResponseDto;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.entity.WorkoutTypeEntity;
import com.project.trainingdiary.exception.user.UserNotFoundException;
import com.project.trainingdiary.exception.workout.WorkoutTypeNotFoundException;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.WorkoutTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkoutTypeService {

  private final WorkoutTypeRepository workoutTypeRepository;
  private final TrainerRepository trainerRepository;

  /*
   * 트레이너의 운동 종류 등록
   */
  public WorkoutTypeResponseDto createWorkoutType(WorkoutTypeCreateRequestDto dto) {
    TrainerEntity trainer = getTrainer();

    WorkoutTypeEntity workoutType = workoutTypeRepository
        .save(WorkoutTypeEntity.toEntity(dto, trainer));

    return WorkoutTypeResponseDto.fromEntity(workoutType);
  }

  /**
   * 트레이너의 운동 종류 수정
   */
  public WorkoutTypeResponseDto updateWorkoutType(WorkoutTypeUpdateRequestDto dto) {
    TrainerEntity trainer = getTrainer();

    WorkoutTypeEntity workoutType = workoutTypeRepository
        .findByTrainer_IdAndId(trainer.getId(), dto.getWorkoutTypeId())
        .orElseThrow(() -> new WorkoutTypeNotFoundException(dto.getWorkoutTypeId()));

    WorkoutTypeEntity updateWorkoutType = workoutTypeRepository
        .save(WorkoutTypeEntity.updateEntity(dto, workoutType));

    return WorkoutTypeResponseDto.fromEntity(updateWorkoutType);
  }

  /**
   * 트레이너의 운동 종류 삭제
   */
  public void deleteWorkoutType(Long workoutTypeId) {
    TrainerEntity trainer = getTrainer();

    WorkoutTypeEntity workoutType = workoutTypeRepository
        .findByTrainer_IdAndId(trainer.getId(), workoutTypeId)
        .orElseThrow(() -> new WorkoutTypeNotFoundException(workoutTypeId));

    workoutTypeRepository.delete(workoutType);
  }

  /**
   * 트레이너의 운동 종류 목록 조회
   */
  public Page<WorkoutTypeResponseDto> getWorkoutTypes(Pageable pageable) {
    TrainerEntity trainer = getTrainer();

    Page<WorkoutTypeEntity> page = workoutTypeRepository
        .findByTrainer_IdOrderByCreatedAtDesc(trainer.getId(), pageable);

    return page.map(WorkoutTypeResponseDto::fromEntity);
  }

  /**
   * 트레이너의 운동 종류 상세 조회
   */
  public WorkoutTypeResponseDto getWorkoutTypeDetails(Long workoutTypeId) {
    TrainerEntity trainer = getTrainer();

    WorkoutTypeEntity entity = workoutTypeRepository
        .findByTrainer_IdAndId(trainer.getId(), workoutTypeId)
        .orElseThrow(() -> new WorkoutTypeNotFoundException(workoutTypeId));

    return WorkoutTypeResponseDto.fromEntity(entity);
  }

  /**
   * 로그인한 트레이너 엔티티
   */
  private TrainerEntity getTrainer() {
    return trainerRepository
        .findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
        .orElseThrow(UserNotFoundException::new);
  }

}
