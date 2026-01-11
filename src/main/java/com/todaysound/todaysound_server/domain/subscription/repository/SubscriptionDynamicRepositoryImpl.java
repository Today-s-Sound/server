package com.todaysound.todaysound_server.domain.subscription.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;

import lombok.RequiredArgsConstructor;

import static com.todaysound.todaysound_server.domain.subscription.entity.QSubscription.subscription;
import static com.todaysound.todaysound_server.domain.subscription.entity.QKeyword.keyword;
import static com.todaysound.todaysound_server.domain.subscription.entity.QSubscriptionKeyword.subscriptionKeyword;

@Repository
@RequiredArgsConstructor
public class SubscriptionDynamicRepositoryImpl implements SubscriptionDynamicRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Subscription> findByUserId(Long userId, Long page, Integer size) {

        return queryFactory
                .selectFrom(subscription)
                .distinct()
                .leftJoin(subscription.subscriptionKeywords, subscriptionKeyword).fetchJoin()
                .leftJoin(subscriptionKeyword.keyword, keyword).fetchJoin()
                .where(subscription.user.id.eq(userId))
                .orderBy(
                        subscription.createdAt.desc(),
                        subscription.id.desc()
                )
                .offset(page * size)
                .limit(size)
                .fetch();

	}

}
