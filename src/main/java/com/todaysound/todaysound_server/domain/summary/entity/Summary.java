package com.todaysound.todaysound_server.domain.summary.entity;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "summaries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Summary extends BaseEntity {

    @Column(name = "hash_tag", nullable = false)
    private String hash;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    /**
     * Summary를 읽음 처리
     */
    public void markAsRead() {
        this.isRead = true;
    }

}
