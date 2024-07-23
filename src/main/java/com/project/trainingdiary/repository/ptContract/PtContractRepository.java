package com.project.trainingdiary.repository.ptContract;

import com.project.trainingdiary.entity.PtContractEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PtContractRepository extends JpaRepository<PtContractEntity, Long>,
    PtContractRepositoryCustom {

  @Query("select case when count(p) > 0 "
      + "then true "
      + "else false "
      + "end "
      + "from pt_contract p "
      + "where p.trainer.id = ?1 "
      + "and p.trainee.id = ?2 "
      + "and p.isTerminated = false")
  boolean existsByTrainerIdAndTraineeId(long trainerId, long traineeId);

  Optional<PtContractEntity> findByIdAndIsTerminatedFalse(long ptContractId);

  @Query("select p "
      + "from pt_contract p "
      + "where p.trainer.id = ?1 "
      + "and p.trainee.id = ?2 "
      + "and p.isTerminated = false")
  Optional<PtContractEntity> findByTrainerIdAndTraineeId(long trainerId, long traineeId);

  @Query("select p "
      + "from pt_contract p "
      + "where p.trainee.id = ?1 "
      + "and p.isTerminated = false")
  Optional<PtContractEntity> findByTraineeId(long traineeId);
}
