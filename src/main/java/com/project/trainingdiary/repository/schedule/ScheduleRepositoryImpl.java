package com.project.trainingdiary.repository.schedule;

import static com.project.trainingdiary.entity.QScheduleEntity.scheduleEntity;
import static com.querydsl.core.types.dsl.Expressions.dateTemplate;

import com.project.trainingdiary.dto.response.ScheduleResponseDto;
import com.project.trainingdiary.model.ScheduleResponseDetail;
import com.project.trainingdiary.model.ScheduleStatus;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ScheduleRepositoryImpl implements ScheduleRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<ScheduleResponseDto> getScheduleList(LocalDateTime startDateTime,
      LocalDateTime endDateTime) {
    List<Tuple> results = queryFactory
        .select(
            scheduleEntity.id,
            startDate(),
            startTime(),
            scheduleEntity.scheduleStatus
        )
        .from(scheduleEntity)
        .where(scheduleEntity.startAt.between(startDateTime, endDateTime))
        .fetch();

    Map<String, List<Tuple>> groupedByDate = results.stream()
        .collect(Collectors.groupingBy(tuple -> tuple.get(startDate())));

    return groupedByDate.entrySet().stream()
        .map(entry -> {
          LocalDate date = LocalDate.parse(entry.getKey());
          List<Tuple> tuples = entry.getValue();
          List<ScheduleResponseDetail> details = tuples.stream()
              .map(tuple -> ScheduleResponseDetail.builder()
                  .id(tuple.get(scheduleEntity.id))
                  .startTime(LocalTime.parse(tuple.get(startTime())))
                  .status(tuple.get(scheduleEntity.scheduleStatus))
                  .build()
              )
              .collect(Collectors.toList());
          boolean existReserved = details.stream()
              .anyMatch(detail -> detail.getStatus() == ScheduleStatus.RESERVED);
          return ScheduleResponseDto.builder()
              .startDate(date)
              .existReserved(existReserved)
              .details(details)
              .build();
        })
        .collect(Collectors.toList());
  }

  // date_format 함수는 MariaDB에서 동작함(H2에서 동작하지 않는 게 확인됨)
  private DateExpression<String> startDate() {
    return dateTemplate(
        String.class,
        "date_format({0}, {1})",
        scheduleEntity.startAt,
        ConstantImpl.create("%Y-%m-%d")
    ).as("startDate");
  }

  private DateExpression<String> startTime() {
    return dateTemplate(
        String.class,
        "date_format({0}, {1})",
        scheduleEntity.startAt,
        ConstantImpl.create("%H:%i") // H는 24시간 기준 2자리. i는 분 2자리
    ).as("startTime");
  }
}
