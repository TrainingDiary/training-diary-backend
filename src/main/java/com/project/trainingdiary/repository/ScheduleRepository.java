package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.ScheduleEntity;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {

  @Query("select s.startAt from schedule s where s.startAt >= ?1 AND s.startAt <= ?2")
  Set<LocalDateTime> findByDates(LocalDateTime startAt1, LocalDateTime startAt2);
}
