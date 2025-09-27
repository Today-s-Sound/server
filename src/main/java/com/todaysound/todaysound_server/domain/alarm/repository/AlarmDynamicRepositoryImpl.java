package com.todaysound.todaysound_server.domain.alarm.repository;

import java.util.List;

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

        List<Long> subscriptionIds = fetchPaginatedSubscriptionIds(userId, pageRequest);

        if (subscriptionIds.isEmpty()) {
            return List.of();
        }

        return queryFactory.select(subscription).from(subscription).distinct()
                .leftJoin(subscription.summaries, summary).fetchJoin()
                .where(subscription.id.in(subscriptionIds), summary.isRead.eq(false))
                .orderBy(subscription.isUrgent.desc(), subscription.updatedAt.desc(),
                        subscription.id.desc())
                .fetch();
    }

    private List<Long> fetchPaginatedSubscriptionIds(Long userId, PageRequestDTO pageRequest) {

        Subscription cursor = findCursor(userId, pageRequest.cursor());

        return queryFactory.select(subscription.id).from(subscription)
                .where(subscription.user.id.eq(userId), hasUnReadSummary(),
                        createCursorCondition(cursor))
                .orderBy(subscription.isUrgent.desc(), subscription.updatedAt.desc(),
                        subscription.id.desc())
                .limit(pageRequest.size()).fetch();
    }

    private Subscription findCursor(Long userId, Long cursorId) {
        if (cursorId == null) {
            return null; // 첫 페이지는 커서가 없음
        }

        Subscription cursor = queryFactory.selectFrom(subscription)
                .where(subscription.user.id.eq(userId), subscription.id.eq(cursorId)).fetchFirst();

        if (cursor == null) {
            throw BaseException.type(AlarmException.ALARM_NOT_FOUND);
        }
        return cursor;
    }

    private BooleanExpression createCursorCondition(Subscription cursor) {

        return subscription.isUrgent.lt(cursor.isUrgent())
                .or(subscription.isUrgent.eq(cursor.isUrgent())
                        .and(subscription.updatedAt.lt(cursor.getUpdatedAt())))
                .or(subscription.isUrgent.eq(cursor.isUrgent())
                        .and(subscription.updatedAt.eq(cursor.getUpdatedAt()))
                        .and(subscription.id.lt(cursor.getId())));
    }

    private BooleanExpression hasUnReadSummary() {
        return JPAExpressions.selectOne().from(summary)
                .where(summary.subscription.id.eq(subscription.id), summary.isRead.eq(false))
                .exists();
    }

}
