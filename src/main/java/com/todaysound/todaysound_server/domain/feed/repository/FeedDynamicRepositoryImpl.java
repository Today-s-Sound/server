package com.todaysound.todaysound_server.domain.feed.repository;

import static com.todaysound.todaysound_server.domain.subscription.entity.QSubscription.subscription;
import static com.todaysound.todaysound_server.domain.summary.entity.QSummary.summary;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.dto.PageRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class FeedDynamicRepositoryImpl implements FeedDynamicRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Summary> findFeeds(Long userId, PageRequest pageRequest) {

        return queryFactory.selectFrom(summary).innerJoin(summary.subscription, subscription).fetchJoin()
                .where(subscription.user.id.eq(userId)).orderBy(summary.updatedAt.desc(), summary.id.desc())
                .offset(pageRequest.page() * pageRequest.size()).limit(pageRequest.size()).fetch();
    }

    @Override
    public List<Summary> findFeedsForHome(Long userId) {

        return queryFactory.selectFrom(summary).innerJoin(summary.subscription, subscription).fetchJoin()
                .where(subscription.user.id.eq(userId)).orderBy(summary.updatedAt.desc(), summary.id.desc()).fetch();
    }


}
