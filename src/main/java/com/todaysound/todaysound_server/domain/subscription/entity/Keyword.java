package com.todaysound.todaysound_server.domain.subscription.entity;


import java.util.ArrayList;
import java.util.List;

import com.todaysound.todaysound_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "keywords")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Keyword extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "keyword", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubscriptionKeyword> subscriptions = new ArrayList<>();


}
