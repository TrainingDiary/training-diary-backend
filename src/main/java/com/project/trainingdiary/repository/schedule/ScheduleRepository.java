package com.project.trainingdiary.repository.schedule;

import com.project.trainingdiary.entity.ScheduleEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long>,
    ScheduleRepositoryCustom {

  @Query("select s.startAt from schedule s where s.startAt >= ?1 AND s.startAt <= ?2")
  Set<LocalDateTime> findScheduleDatesByDates(LocalDateTime startAt1, LocalDateTime startAt2);

  @Query("select s from schedule s where s.startAt >= ?1 AND s.startAt <= ?2")
  List<ScheduleEntity> findByDates(LocalDateTime startAt1, LocalDateTime startAt2);
}
