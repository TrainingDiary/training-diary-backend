package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.VerificationEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationEntity, Long> {

  Optional<VerificationEntity> findByEmail(String email);
}
