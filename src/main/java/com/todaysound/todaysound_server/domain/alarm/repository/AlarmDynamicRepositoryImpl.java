package com.todaysound.todaysound_server.domain.alarm.repository;

import java.util.List;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.exception.BaseException;
import org.springframework.stereotype.Repository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todaysound.todaysound_server.domain.alarm.exception.AlarmException;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.global.dto.PageRequestDTO;
import lombok.RequiredArgsConstructor;

import static com.todaysound.todaysound_server.domain.subscription.entity.QSubscription.subscription;
import static com.todaysound.todaysound_server.domain.summary.entity.QSummary.summary;


@Repository
@RequiredArgsConstructor
public class AlarmDynamicRepositoryImpl implements AlarmDynamicRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Summary> findAlarms(Long userId, PageRequestDTO pageRequest) {

        return queryFactory.selectFrom(summary).innerJoin(summary.subscription, subscription).fetchJoin()
                .where(subscription.user.id.eq(userId), subscription.isAlarmEnabled.eq(true),
                        summary.isKeywordMatched.eq(true)).orderBy(summary.updatedAt.desc(), summary.id.desc())
                .offset(pageRequest.page() * pageRequest.size()).limit(pageRequest.size()).fetch();
    }


}
