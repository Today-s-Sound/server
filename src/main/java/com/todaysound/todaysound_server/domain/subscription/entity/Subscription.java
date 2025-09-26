package com.todaysound.todaysound_server.domain.subscription.entity;

import com.todaysound.todaysound_server.domain.auth.entity.User;
import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(name = "subscriptions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "alias", nullable = false)
    private String alias;

    @Column(name = "is_urgent", nullable = false)
    private boolean isUrgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "subscription")
    @OnDelete(action = OnDeleteAction.CASCADE)
    List<SubscriptionKeyword> subscriptionKeywords = new ArrayList<>();

    @OneToMany(mappedBy = "subscription")
    @OnDelete(action = OnDeleteAction.CASCADE)
    List<Summary> summaries = new ArrayList<>();

}
