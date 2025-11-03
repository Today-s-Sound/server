package com.todaysound.todaysound_server.domain.subscription.entity;

import com.todaysound.todaysound_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@Table(name = "keywords")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Keyword extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Builder.Default
    @OneToMany(mappedBy = "keyword", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubscriptionKeyword> subscriptions = new ArrayList<>();

}
