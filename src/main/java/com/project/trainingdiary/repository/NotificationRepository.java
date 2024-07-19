package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

  Page<NotificationEntity> findByTrainer_Id(long id, Pageable pageable);

  Page<NotificationEntity> findByTrainee_Id(long id, Pageable pageable);
}
