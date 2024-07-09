package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.WorkoutSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSessionEntity, Long> {

}
