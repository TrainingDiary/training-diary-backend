package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.WorkoutTypeEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutTypeRepository extends JpaRepository<WorkoutTypeEntity, Long> {

  Page<WorkoutTypeEntity> findByTrainer_Id(Long id, Pageable pageable);

  Optional<WorkoutTypeEntity> findByTrainer_IdAndId(Long trainerId, Long workoutTypeId);

}
