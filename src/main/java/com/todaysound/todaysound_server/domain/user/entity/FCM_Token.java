package com.todaysound.todaysound_server.domain.user.entity;

import com.todaysound.todaysound_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@Table(name = "fcm_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FCM_Token extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "fcm_token", nullable = false, length = 255)
    private String fcmToken;

    @Column(name = "model", nullable = false, length = 100)
    private String model;




}
