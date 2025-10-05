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
	public List<Subscription> findByUserId(Long userId, Long cursor, Integer size) {

		List<Long> subscriptionIds = fetchSubscriptionIds(userId, cursor, size);

		if (subscriptionIds.isEmpty()) {
			return List.of();
		}

		return queryFactory.selectFrom(subscription).distinct()
				.leftJoin(subscription.subscriptionKeywords, subscriptionKeyword).fetchJoin()
				.leftJoin(subscriptionKeyword.keyword, keyword).fetchJoin()
				.where(subscription.id.in(subscriptionIds)).orderBy(subscription.isUrgent.desc(),
						subscription.createdAt.desc(), subscription.id.desc())
				.fetch();

	}

	private List<Long> fetchSubscriptionIds(Long userId, Long cursor, Integer size) {
		return queryFactory.select(subscription.id).from(subscription)
				.where(subscription.user.id.eq(userId), getPaginationConditions(cursor))
				.orderBy(subscription.isUrgent.desc(), subscription.createdAt.desc(),
						subscription.id.desc())
				.limit(size).fetch();
	}


	private BooleanExpression getPaginationConditions(Long cursor) {
		if (cursor == null) {
			return null;
		}
		
		LocalDateTime cursorCreatedAt = getCursorCreatedAt(cursor);
		if (cursorCreatedAt == null) {
			return null;
		}
		
		return subscription.createdAt.loe(cursorCreatedAt)
				.and(subscription.id.lt(cursor));
	}

	private LocalDateTime getCursorCreatedAt(final Long cursor) {
		if (cursor == null) {
			return null;
		}
		return queryFactory.select(subscription.createdAt).from(subscription)
				.where(subscription.id.eq(cursor)).fetchFirst();
	}
}
