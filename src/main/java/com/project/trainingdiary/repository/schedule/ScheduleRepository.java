package com.project.trainingdiary.repository.schedule;

import com.project.trainingdiary.entity.ScheduleEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long>,
    ScheduleRepositoryCustom {

  @Query("select s.startAt "
      + "from schedule s "
      + "where s.trainer.id = :id "
      + "and s.startAt >= :startAt1 "
      + "and s.startAt <= :startAt2")
  Set<LocalDateTime> findScheduleDatesByDates(
      @Param("id") long id,
      @Param("startAt1") LocalDateTime startAt1,
      @Param("startAt2") LocalDateTime startAt2
  );

  @Query("select s "
      + "from schedule s "
      + "where s.trainer.id = :id "
      + "and s.startAt >= :startAt1 "
      + "and s.startAt <= :startAt2")
  List<ScheduleEntity> findByDates(
      @Param("id") long id,
      @Param("startAt1") LocalDateTime startAt1,
      @Param("startAt2") LocalDateTime startAt2
  );

  @Query("select s "
      + "from schedule s "
      + "left join fetch s.trainer "
      + "left join fetch s.ptContract "
      + "left join fetch s.ptContract.trainee "
      + "where s.startAt >= :startAt1 "
      + "and s.startAt <= :startAt2")
  List<ScheduleEntity> findByDatesWithDetails(
      @Param("startAt1") LocalDateTime startAt1,
      @Param("startAt2") LocalDateTime startAt2
  );

  @Query("select s "
      + "from schedule s "
      + "where s.trainer.id = :trainerId "
      + "and s.ptContract.trainee.id = :traineeId "
      + "and s.startAt >= :startAt")
  List<ScheduleEntity> findByTraineeIdAndDateAfter(
      @Param("trainerId") long trainerId,
      @Param("traineeId") long traineeId,
      @Param("startAt") LocalDateTime startAt
  );
}
