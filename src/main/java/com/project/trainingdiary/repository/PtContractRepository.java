package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.PtContractEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PtContractRepository extends JpaRepository<PtContractEntity, Long> {

  boolean existsByTrainerIdAndTraineeId(long trainerId, long traineeId);

  List<PtContractEntity> findByTrainee_Email(String email);

  List<PtContractEntity> findByTrainer_Email(String email);
}
