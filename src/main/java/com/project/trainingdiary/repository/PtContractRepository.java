package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.PtContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PtContractRepository extends JpaRepository<PtContractEntity, Long> {

  boolean existsByTrainerIdAndTraineeId(long trainerId, long traineeId);
}
