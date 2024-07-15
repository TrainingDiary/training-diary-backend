package com.project.trainingdiary.repository.ptContract;

import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.model.PtContractSort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PtContractRepositoryCustom {

  Page<PtContractEntity> findByTraineeEmail(String email, Pageable pageable, PtContractSort sortBy);

  Page<PtContractEntity> findByTrainerEmail(String email, Pageable pageable, PtContractSort sortBy);
}
