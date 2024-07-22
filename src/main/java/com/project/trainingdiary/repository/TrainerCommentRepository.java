package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.TrainerCommentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerCommentRepository extends JpaRepository<TrainerCommentEntity, Long> {

  List<TrainerCommentEntity> findByDietId(Long id);

}
