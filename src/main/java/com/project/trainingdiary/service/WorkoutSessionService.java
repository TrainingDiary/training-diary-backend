package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.WorkoutSessionCreateRequestDto;
import com.project.trainingdiary.dto.response.WorkoutSessionListResponseDto;
import com.project.trainingdiary.dto.response.WorkoutSessionResponseDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.entity.WorkoutEntity;
import com.project.trainingdiary.entity.WorkoutSessionEntity;
import com.project.trainingdiary.entity.WorkoutTypeEntity;
import com.project.trainingdiary.exception.impl.PtContractNotFoundException;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.exception.impl.WorkoutSessionNotFoundException;
import com.project.trainingdiary.exception.impl.WorkoutTypeNotFoundException;
import com.project.trainingdiary.repository.PtContractRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.WorkoutRepository;
import com.project.trainingdiary.repository.WorkoutSessionRepository;
import com.project.trainingdiary.repository.WorkoutTypeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutSessionService {

  private final WorkoutTypeRepository workoutTypeRepository;
  private final WorkoutSessionRepository workoutSessionRepository;
  private final WorkoutRepository workoutRepository;
  private final TrainerRepository trainerRepository;
  private final PtContractRepository ptContractRepository;

  /**
   * 트레이너의 운동 일지 생성
   */
  @Transactional
  public void createWorkoutSession(WorkoutSessionCreateRequestDto dto) {
    TrainerEntity trainer = trainerRepository
        .findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
        .orElseThrow(UserNotFoundException::new);

    PtContractEntity ptContract = ptContractRepository
        .findByTrainerIdAndTraineeId(trainer.getId(), dto.getTraineeId())
        .orElseThrow(PtContractNotFoundException::new);

    List<WorkoutEntity> workouts = dto.getWorkouts().stream().map(details -> {
      WorkoutTypeEntity workoutType = workoutTypeRepository.findById(details.getWorkoutTypeId())
          .orElseThrow(() -> new WorkoutTypeNotFoundException(details.getWorkoutTypeId()));
      WorkoutEntity workout = WorkoutEntity.toEntity(details, workoutType);
      workoutRepository.save(workout);
      return workout;
    }).toList();

    workoutSessionRepository.save(WorkoutSessionEntity.toEntity(dto, workouts, ptContract));
  }

  /**
   * 운동 일지 목록 조회
   */
  public Page<WorkoutSessionListResponseDto> getWorkoutSessions(Long traineeId, Pageable pageable) {
    return workoutSessionRepository
        .findByPtContract_Trainee_IdOrderBySessionDateDesc(traineeId, pageable)
        .map(WorkoutSessionListResponseDto::fromEntity);
  }

  /**
   * 운동 일지 상세 조회
   */
  public WorkoutSessionResponseDto getWorkoutSessionDetails(Long traineeId, Long sessionId) {
    WorkoutSessionEntity session = workoutSessionRepository
        .findByIdAndPtContract_Trainee_Id(sessionId, traineeId)
        .orElseThrow(() -> new WorkoutSessionNotFoundException(sessionId));
    return WorkoutSessionResponseDto.fromEntity(session);
  }

}
