package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.PtContractEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PtContractRepository extends JpaRepository<PtContractEntity, Long> {

  boolean existsByTrainerIdAndTraineeId(long trainerId, long traineeId);

  Page<PtContractEntity> findByTrainee_Email(String email, Pageable pageable);

  Page<PtContractEntity> findByTrainer_Email(String email, Pageable pageable);
}
