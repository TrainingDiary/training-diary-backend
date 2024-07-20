package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.FcmTokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmTokenRepository extends JpaRepository<FcmTokenEntity, Long> {

  Optional<FcmTokenEntity> findByTraineeId(long id);

  Optional<FcmTokenEntity> findByTrainerId(long id);
}
