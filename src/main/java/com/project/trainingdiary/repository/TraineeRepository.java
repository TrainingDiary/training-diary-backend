package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.TraineeEntity;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TraineeRepository extends JpaRepository<TraineeEntity, Long> {

  Optional<TraineeEntity> findByEmail(String email);

  @Query("SELECT t FROM trainee t LEFT JOIN FETCH t.inBodyRecords WHERE t.id = :id")
  Optional<TraineeEntity> findByIdWithInBodyRecords(@Param("id") Long id);
}
