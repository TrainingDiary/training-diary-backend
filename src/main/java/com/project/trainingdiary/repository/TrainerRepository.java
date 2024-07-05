package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.TrainerEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<TrainerEntity, Long> {

  Optional<TrainerEntity> findByEmail(String email);

  Optional<TrainerEntity> findById(Long id);

}
