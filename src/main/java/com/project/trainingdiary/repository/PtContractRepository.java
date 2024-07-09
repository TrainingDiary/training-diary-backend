package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.PtContractEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PtContractRepository extends JpaRepository<PtContractEntity, Long> {

  boolean existsByTrainerIdAndTraineeId(long trainerId, long traineeId);

  Optional<PtContractEntity> findByTrainerIdAndTraineeId(long trainerId, long traineeId);
}
