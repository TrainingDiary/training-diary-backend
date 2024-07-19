package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.DietEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DietRepository extends JpaRepository<DietEntity, Long> {

  Page<DietEntity> findByTraineeId(Long traineeId, Pageable pageable);
}
