package com.todaysound.todaysound_server.domain.alram.repository;


import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import lombok.RequiredArgsConstructor;

import static com.todaysound.todaysound_server.domain.subscription.entity.QSubscription.subscription;
import static com.todaysound.todaysound_server.domain.summary.entity.QSummary.summary;


@Repository
@RequiredArgsConstructor
public class AlramDynamicRepositoryImpl implements AlramDynamicRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Subscription> findSubscriptionWithUnreadSummaries(Long userId, Pageable pageable) {

        List<Long> subscriptionIds =
                fetchSubscriptionIds(userId, pageable.getOffset(), pageable.getPageSize());

        return queryFactory.select(subscription).from(subscription).distinct()
                .leftJoin(subscription.summaries, summary).fetchJoin()
                .where(subscription.id.in(subscriptionIds)).where(summary.isRead.eq(false))
                .orderBy(subscription.isUrgent.desc(), subscription.updatedAt.desc(),
                        summary.updatedAt.desc())
                .fetch();
    }

    private List<Long> fetchSubscriptionIds(Long userId, Long offset, Integer size) {

        return queryFactory.select(subscription.id).from(subscription)
                .where(subscription.user.id.eq(userId), hasUnReadSummary())
                .orderBy(subscription.isUrgent.desc(), subscription.updatedAt.desc()).offset(offset)
                .limit(size).fetch();
    }

    private BooleanExpression hasUnReadSummary() {
        return JPAExpressions.selectOne().from(summary)
                .where(summary.subscription.id.eq(subscription.id), summary.isRead.eq(false))
                .exists();
    }

}
