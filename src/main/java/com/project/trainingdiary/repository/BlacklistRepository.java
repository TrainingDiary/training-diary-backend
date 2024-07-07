package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.BlacklistedTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlacklistRepository extends JpaRepository<BlacklistedTokenEntity, Long> {
    Optional<BlacklistedTokenEntity> findByToken(String token);
}
