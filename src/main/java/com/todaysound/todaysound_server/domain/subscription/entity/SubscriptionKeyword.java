package com.todaysound.todaysound_server.domain.subscription.entity;

import com.todaysound.todaysound_server.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "subscriptions_keywords")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionKeyword extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id", nullable = false)
  private Subscription subscription;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "keyword_id", nullable = false)
  private Keyword keyword;

}
