package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.BlacklistedTokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistRepository extends JpaRepository<BlacklistedTokenEntity, Long> {

  Optional<BlacklistedTokenEntity> findByToken(String token);
}
