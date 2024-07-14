package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.entity.WorkoutSessionEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSessionEntity, Long> {

  Page<WorkoutSessionEntity> findByPtContract_Trainee_IdOrderBySessionDateDesc(Long traineeId,
      Pageable pageable);

  Optional<WorkoutSessionEntity> findByIdAndPtContract_Trainee_Id(Long sessionId, Long traineeId);

  Optional<WorkoutSessionEntity> findByPtContract_TrainerAndId(TrainerEntity trainer,
      Long sessionId);

}
