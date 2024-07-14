package com.project.trainingdiary.repository.ptContract;

import static com.project.trainingdiary.entity.QPtContractEntity.ptContractEntity;

import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.model.PtContractSort;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@AllArgsConstructor
public class PtContractRepositoryImpl implements PtContractRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<PtContractEntity> findByTraineeEmail(String email, Pageable pageable,
      PtContractSort sortBy) {
    List<PtContractEntity> content = queryFactory
        .selectFrom(ptContractEntity)
        .join(ptContractEntity.trainer).fetchJoin()
        .join(ptContractEntity.trainee).fetchJoin()
        .where(
            ptContractEntity.trainee.email.eq(email),
            ptContractEntity.isTerminated.isFalse()
        )
        .orderBy(order(sortBy))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    Long total = queryFactory
        .select(ptContractEntity.count())
        .from(ptContractEntity)
        .join(ptContractEntity.trainer)
        .join(ptContractEntity.trainee)
        .where(
            ptContractEntity.trainee.email.eq(email),
            ptContractEntity.isTerminated.isFalse()
        )
        .fetchOne();

    return new PageImpl<>(content, pageable, Optional.ofNullable(total).orElse(0L));
  }

  @Override
  public Page<PtContractEntity> findByTrainerEmail(String email, Pageable pageable,
      PtContractSort sortBy) {
    List<PtContractEntity> content = queryFactory
        .selectFrom(ptContractEntity)
        .join(ptContractEntity.trainer).fetchJoin()
        .join(ptContractEntity.trainee).fetchJoin()
        .where(
            ptContractEntity.trainer.email.eq(email),
            ptContractEntity.isTerminated.isFalse()
        )
        .orderBy(order(sortBy))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    Long total = queryFactory
        .select(ptContractEntity.count())
        .from(ptContractEntity)
        .join(ptContractEntity.trainer)
        .join(ptContractEntity.trainee)
        .where(
            ptContractEntity.trainer.email.eq(email),
            ptContractEntity.isTerminated.isFalse()
        )
        .fetchOne();

    return new PageImpl<>(content, pageable, Optional.ofNullable(total).orElse(0L));
  }

  private OrderSpecifier<?> order(PtContractSort sortBy) {
    if (sortBy == PtContractSort.NAME) {
      return new OrderSpecifier<>(
          Order.ASC,
          ptContractEntity.trainee.name
      );
    } else if (sortBy == PtContractSort.SESSION_UPDATED_AT) {
      return new OrderSpecifier<>(
          Order.DESC,
          ptContractEntity.totalSessionUpdatedAt
      );
    } else {
      return null;
    }
  }
}
