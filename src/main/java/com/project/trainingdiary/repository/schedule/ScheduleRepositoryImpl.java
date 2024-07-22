package com.project.trainingdiary.repository.schedule;

import static com.project.trainingdiary.entity.QScheduleEntity.scheduleEntity;
import static com.querydsl.core.types.dsl.Expressions.dateTemplate;

import com.project.trainingdiary.dto.response.schedule.ScheduleResponseDto;
import com.project.trainingdiary.model.ScheduleResponseDetail;
import com.project.trainingdiary.model.type.ScheduleStatusType;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ScheduleRepositoryImpl implements ScheduleRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  /**
   * 트레이너가 조회할 때는 트레이너의 모든 일정과 수강생의 이름을 표시함
   */
  @Override
  public List<ScheduleResponseDto> getScheduleListByTrainer(
      long trainerId,
      LocalDateTime startDateTime,
      LocalDateTime endDateTime
  ) {
    return getScheduleList(trainerId, startDateTime, endDateTime, null);
  }

  /**
   * 트레이니가 조회할 때는 연결된 트레이너의 모든 일정을 보여주는 것은 같지만, traineeId를 검사해서 조회를 요청한 트레이니의 일정이 아닌 것은 이름을 가려야 함
   */
  @Override
  public List<ScheduleResponseDto> getScheduleListByTrainee(
      long trainerId,
      long traineeId,
      LocalDateTime startDateTime,
      LocalDateTime endDateTime
  ) {
    return getScheduleList(trainerId, startDateTime, endDateTime, traineeId);
  }

  private List<ScheduleResponseDto> getScheduleList(
      long trainerId,
      LocalDateTime startDateTime,
      LocalDateTime endDateTime,
      Long includeOnlyThisTraineeId
  ) {
    List<Tuple> results = queryFactory
        .select(
            scheduleEntity,
            scheduleEntity.id,
            startDate(),
            startTime(),
            scheduleEntity.scheduleStatusType,
            scheduleEntity.trainer.id,
            scheduleEntity.trainer.name,
            scheduleEntity.ptContract.trainee.id,
            scheduleEntity.ptContract.trainee.name
        )
        .from(scheduleEntity)
        .join(scheduleEntity.trainer).fetchJoin()
        // status가 OPEN일 때는 ptContract가 없지만, 그 경우도 모두 찾아야 하므로 left join
        .leftJoin(scheduleEntity.ptContract).fetchJoin()
        .leftJoin(scheduleEntity.ptContract.trainee).fetchJoin()
        .where(
            scheduleEntity.trainer.id.eq(trainerId),
            scheduleEntity.startAt.between(startDateTime, endDateTime)
        )
        .orderBy(scheduleEntity.startAt.asc())
        .fetch();

    // 날짜별로 결과를 묶기(날짜로 정렬)
    SortedMap<String, List<Tuple>> groupedByDate = new TreeMap<>();
    for (Tuple tuple : results) {
      String date = tuple.get(startDate());
      List<Tuple> timeList = groupedByDate.getOrDefault(date, new ArrayList<>());
      timeList.add(tuple);
      groupedByDate.put(date, timeList);
    }

    // 묶은 결과를 DTO에 맞게 변형
    return groupedByDate.entrySet().stream()
        .map(entry -> {
          List<Tuple> tuples = entry.getValue();

          // 해당 날짜
          LocalDate date = LocalDate.parse(entry.getKey());

          // 해당 날짜 안의 각 시간 정보
          List<ScheduleResponseDetail> details = tuples.stream()
              .map(tuple -> ScheduleResponseDetail.builder()
                  .scheduleId(tuple.get(scheduleEntity.id))
                  .trainerId(tuple.get(scheduleEntity.trainer.id))
                  .trainerName(tuple.get(scheduleEntity.trainer.name))
                  .traineeId(getTraineeId(tuple, includeOnlyThisTraineeId))
                  .traineeName(getTraineeName(tuple, includeOnlyThisTraineeId))
                  .startTime(LocalTime.parse(Objects.requireNonNull(tuple.get(startTime()))))
                  .status(tuple.get(scheduleEntity.scheduleStatusType))
                  .build()
              )
              .collect(Collectors.toList());

          // 해당 날짜에 예약이 존재하는지 검사
          boolean existReserved = details.stream()
              .anyMatch(detail -> detail.getStatus() == ScheduleStatusType.RESERVED);

          return ScheduleResponseDto.builder()
              .startDate(date)
              .existReserved(existReserved)
              .details(details)
              .build();
        })
        .collect(Collectors.toList());
  }

  private static Long getTraineeId(Tuple tuple, Long includeOnlyThisTraineeId) {
    Long traineeId = tuple.get(scheduleEntity.ptContract.trainee.id);
    return includeOnlyThisTraineeId == null || includeOnlyThisTraineeId.equals(traineeId)
        ? traineeId
        : null;
  }

  private static String getTraineeName(Tuple tuple, Long includeOnlyThisTraineeId) {
    Long traineeId = tuple.get(scheduleEntity.ptContract.trainee.id);
    return includeOnlyThisTraineeId == null || includeOnlyThisTraineeId.equals(traineeId)
        ? tuple.get(scheduleEntity.ptContract.trainee.name)
        : null;
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
