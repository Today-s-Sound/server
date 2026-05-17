package com.todaysound.todaysound_server.domain.subscription.repository;

import static com.todaysound.todaysound_server.domain.subscription.entity.QKeyword.keyword;
import static com.todaysound.todaysound_server.domain.subscription.entity.QSubscription.subscription;
import static com.todaysound.todaysound_server.domain.subscription.entity.QSubscriptionKeyword.subscriptionKeyword;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SubscriptionDynamicRepositoryImpl implements SubscriptionDynamicRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Subscription> findByUserId(Long userId, Integer page, Integer size) {

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
