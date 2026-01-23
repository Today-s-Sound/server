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

        // 알람을 바꿨을 때 그 때를 기억해서 그 전에 목록은 안보이는게 맞다
        return queryFactory.selectFrom(summary)
                .innerJoin(summary.subscription, subscription).fetchJoin()
                .where(
                        subscription.user.id.eq(userId),
                        subscription.isAlarmEnabled.eq(true),
                        subscription.lastAlarmToggleAt.isNull()
                                .or(summary.createdAt.goe(subscription.lastAlarmToggleAt))
                )
                .orderBy(summary.updatedAt.desc(), summary.id.desc())
                .offset(pageRequest.page() * pageRequest.size())
                .limit(pageRequest.size()).fetch();
    }


}
