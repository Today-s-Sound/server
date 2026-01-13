package com.todaysound.todaysound_server.domain.alarm.repository;

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
public class AlarmDynamicRepositoryImpl implements AlarmDynamicRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Summary> findAlarms(Long userId, PageRequest pageRequest) {

        return queryFactory.selectFrom(summary).innerJoin(summary.subscription, subscription).fetchJoin()
                .where(subscription.user.id.eq(userId), subscription.isAlarmEnabled.eq(true))
                .orderBy(summary.updatedAt.desc(), summary.id.desc()).offset(pageRequest.page() * pageRequest.size())
                .limit(pageRequest.size()).fetch();
    }


}
