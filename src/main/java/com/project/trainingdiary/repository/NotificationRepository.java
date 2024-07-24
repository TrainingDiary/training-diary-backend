package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.NotificationEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

  Page<NotificationEntity> findByTrainer_Id(long id, Pageable pageable);

  Page<NotificationEntity> findByTrainee_Id(long id, Pageable pageable);

  @Query("select n from notification n "
      + "left join fetch n.trainer "
      + "left join fetch n.trainee "
      + "left join fetch n.trainer.fcmToken "
      + "left join fetch n.trainee.fcmToken "
      + "where n.id = :id"
  )
  Optional<NotificationEntity> findByIdWithToken(long id);
}
