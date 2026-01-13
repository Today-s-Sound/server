package com.todaysound.todaysound_server.domain.summary.entity;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "summaries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Summary extends BaseEntity {

    @Column(name = "hash_tag", nullable = false)
    private String hash;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "post_url", nullable = false)
    private String postUrl;

    @Column(name = "post_date")
    private String postDate;

    @Column(name = "is_keyword_matched", nullable = false)
    private boolean isKeywordMatched;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    // Summary 생성 팩토리 메서드
    public static Summary create(String hash, String title, String content,
                                 String postUrl, String postDate, boolean isKeywordMatched,
                                 Subscription subscription) {
        Summary summary = new Summary();
        summary.hash = hash;
        summary.title = title;
        summary.content = content;
        summary.postUrl = postUrl;
        summary.postDate = postDate;
        summary.isKeywordMatched = isKeywordMatched;
        summary.createdAt = LocalDateTime.now();
        summary.updatedAt = LocalDateTime.now();
        summary.subscription = subscription;
        return summary;
    }

    // Summary를 읽음 처리
    public void markAsRead() {
        this.isKeywordMatched = true;
        this.updatedAt = LocalDateTime.now();
    }

}
