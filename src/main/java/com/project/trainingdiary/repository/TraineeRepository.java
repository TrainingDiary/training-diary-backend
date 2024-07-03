package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.TraineeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraineeRepository extends JpaRepository<TraineeEntity, Long> {

  Optional<TraineeEntity> findByEmail(String email);
}
