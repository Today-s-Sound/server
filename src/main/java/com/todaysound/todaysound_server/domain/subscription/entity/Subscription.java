package com.todaysound.todaysound_server.domain.subscription.entity;

import com.todaysound.todaysound_server.domain.summary.entity.Summary;
import com.todaysound.todaysound_server.domain.url.entity.Url;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Builder
@Table(name = "subscriptions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Subscription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "alias", nullable = false)
    private String alias;

    @Column(name = "is_alarm_enabled", nullable = false)
    private boolean isAlarmEnabled;

    // 마지막으로 처리한 게시물의 site_post_id
    @Builder.Default
    @Column(name = "last_seen_post_id", nullable = false)
    private String lastSeenPostId = "";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private Url url;

    @Builder.Default
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<SubscriptionKeyword> subscriptionKeywords = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Summary> summaries = new ArrayList<>();

    public void updateLastSeenPostId(String lastSeenPostId) {
        this.lastSeenPostId = lastSeenPostId;
    }

    public void alarmBlock() {
        this.isAlarmEnabled = false;
    }

    public void alarmUnblock() {
        this.isAlarmEnabled = true;
    }

    public void updateAlias(String alias) {
        if (alias != null) {
            this.alias = alias;
        }
    }

    public void updateIsAlarmEnabled(Boolean alarmEnabled) {
        if (alarmEnabled != null) {
            this.isAlarmEnabled = alarmEnabled;
        }
    }

    public void updateKeywords(List<Keyword> keywords) {
        // orphanRemoval = true로 기존 키워드 삭제
        this.subscriptionKeywords.clear();

        keywords.forEach(keyword -> this.subscriptionKeywords.add(SubscriptionKeyword.of(this, keyword)));
    }
}
