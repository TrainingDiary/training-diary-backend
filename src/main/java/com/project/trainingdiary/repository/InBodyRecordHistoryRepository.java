package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.InBodyRecordHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InBodyRecordHistoryRepository extends
    JpaRepository<InBodyRecordHistoryEntity, Long> {

}
