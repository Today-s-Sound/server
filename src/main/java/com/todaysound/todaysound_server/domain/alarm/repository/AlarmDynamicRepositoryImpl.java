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
        public List<Subscription> findSubscriptionWithUnreadSummaries(Long userId,
                        PageRequestDTO pageRequest) {

                return queryFactory.selectFrom(subscription).distinct()
                                .leftJoin(subscription.summaries, summary).fetchJoin()
                                .where(subscription.user.id.eq(userId), hasUnReadSummary())
                                .orderBy(subscription.isUrgent.desc(),
                                                subscription.updatedAt.desc(),
                                                subscription.id.desc())
                                .offset(pageRequest.page() * pageRequest.size())
                                .limit(pageRequest.size()).fetch();
        }


        @Override
        public List<Summary> findUnreadSummariesAndIsAlarmEnabledByUserId(Long userId,
                        PageRequestDTO pageRequest) {

                return queryFactory.selectFrom(summary)
                                .innerJoin(summary.subscription, subscription).fetchJoin()
                                .where(subscription.user.id.eq(userId))
                                .orderBy(subscription.isUrgent.desc(), summary.updatedAt.desc(),
                                                summary.id.desc())
                                .offset(pageRequest.page() * pageRequest.size())
                                .limit(pageRequest.size()).fetch();
        }



        private BooleanExpression hasUnReadSummary() {
                return JPAExpressions.selectOne().from(summary)
                                .where(summary.subscription.id.eq(subscription.id),
                                                summary.isRead.eq(false))
                                .exists();
        }

}
