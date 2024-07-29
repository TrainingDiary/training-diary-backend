package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.DietEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DietRepository extends JpaRepository<DietEntity, Long> {

  Optional<DietEntity> findByTraineeIdAndId(Long id, Long id1);

  @Query("SELECT d FROM diet d " +
      "LEFT JOIN FETCH d.comments c " +
      "LEFT JOIN FETCH c.trainer " +
      "WHERE d.id = :id")
  Optional<DietEntity> findByIdWithCommentsAndTrainer(@Param("id") Long id);

  @Query("SELECT d FROM diet d " +
      "LEFT JOIN FETCH d.comments c " +
      "LEFT JOIN FETCH c.trainer " +
      "WHERE d.id = :id AND d.trainee.id = :traineeId")
  Optional<DietEntity> findByTraineeIdAndIdWithCommentsAndTrainer(
      @Param("traineeId") Long traineeId, @Param("id") Long id);

  @Query("SELECT d FROM diet d WHERE d.trainee.id = :traineeId ORDER BY d.createdAt DESC")
  Page<DietEntity> findDietImagesByTraineeId(@Param("traineeId") Long traineeId, Pageable pageable);
}
