package com.todaysound.todaysound_server.domain.url.entity;

import com.todaysound.todaysound_server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "urls")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Url extends BaseEntity {

    @Column
    String link;

    @Column
    String title;

}
