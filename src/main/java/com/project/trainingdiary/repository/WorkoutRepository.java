package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.WorkoutEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRepository extends JpaRepository<WorkoutEntity, Long> {

}
