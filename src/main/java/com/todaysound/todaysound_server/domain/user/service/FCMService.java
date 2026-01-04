package com.todaysound.todaysound_server.domain.user.service;

import com.google.firebase.messaging.*;
import com.todaysound.todaysound_server.domain.user.entity.FCM_Token;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.repository.FCMRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    private final FCMRepository fcmRepository;

    /**
     * (핵심 메소드) 특정 User에게 알림을 발송합니다.
     * 
     * @param user 알림을 받을 User 엔티티
     * @param title 알림 제목
     * @param body 알림 본문
     */
    @Transactional
    public void sendNotificationToUser(User user, String title, String body) {

        List<FCM_Token> devices = fcmRepository.findByUser(user);

        if (devices.isEmpty()) {
            log.warn("알림을 보낼 기기(토큰)가 없습니다. (User ID: {})", user.getId());
            return;
        }

        // FCM 토큰 문자열만 추출
        List<String> tokens =
                devices.stream().map(FCM_Token::getFcmToken).collect(Collectors.toList());

        // 알림 메시지 내용 구성
        Notification notification = Notification.builder().setTitle(title).setBody(body).build();

        ApnsConfig apnsConfig = ApnsConfig.builder()
                .putHeader("apns-priority", "10")
                .setAps(Aps.builder().setSound("default").setBadge(1).build())
                .build();

        // 여러 토큰에 한 번에 보내는 MulticastMessage 구성
        MulticastMessage message = MulticastMessage.builder().setNotification(notification)
                .setApnsConfig(apnsConfig).addAllTokens(tokens).build();

        // FCM에 일괄 발송 요청
        BatchResponse response;
        try {
            response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            log.info("총 {}건의 알림 발송 요청 성공. (성공: {}건, 실패: {}건)",
                    response.getSuccessCount() + response.getFailureCount(),
                    response.getSuccessCount(), response.getFailureCount());

            if (response.getFailureCount() > 0) {
                handleFailedTokens(response, tokens);
            }

        } catch (FirebaseMessagingException e) {
            log.error("FCM Multicast 발송 실패", e);
        }
    }

    /**
     * 발송 실패(특히 UNREGISTERED) 응답을 받은 토큰을 DB에서 삭제
     */
    private void handleFailedTokens(BatchResponse response, List<String> originalTokens) {
        List<String> tokensToDelete = new ArrayList<>();

        List<SendResponse> responses = response.getResponses();

        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);

            // 발송 실패한 경우 처리
            if (!sendResponse.isSuccessful()) {
                String failedToken = originalTokens.get(i);
                MessagingErrorCode errorCode = sendResponse.getException().getMessagingErrorCode();

                if (errorCode == MessagingErrorCode.UNREGISTERED) {
                    log.warn("FCM 토큰 {}이(가) 만료(UNREGISTERED)되었습니다. DB 삭제 목록에 추가합니다.", failedToken);
                    tokensToDelete.add(failedToken);
                } else {
                    log.warn("FCM 토큰 {} 발송 실패. (에러 코드: {})", failedToken, errorCode);
                }
            }
        }

        // 삭제할 토큰이 있다면 DB에서 일괄 삭제
        if (!tokensToDelete.isEmpty()) {
            fcmRepository.deleteAllByFcmTokenIn(tokensToDelete);
            log.info("만료된 FCM 토큰 {}건을 DB에서 삭제했습니다.", tokensToDelete.size());
        }
    }
}
