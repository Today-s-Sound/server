package com.todaysound.todaysound_server.global.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.IncomingHttpResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import com.todaysound.todaysound_server.domain.user.entity.FCM_Token;
import com.todaysound.todaysound_server.domain.user.entity.User;
import com.todaysound.todaysound_server.domain.user.entity.UserType;
import com.todaysound.todaysound_server.domain.user.repository.FCMRepository;
import com.todaysound.todaysound_server.domain.user.repository.UserRepository;
import com.todaysound.todaysound_server.global.utils.CryptoUtils;
import com.todaysound.todaysound_server.support.ServiceTestSupport;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class FCMServiceTest extends ServiceTestSupport {

    private static final String EVENT_ID = "a".repeat(64);

    @Autowired
    private FCMService fcmService;

    @Autowired
    private FCMRepository fcmRepository;

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("sendNotificationToUser 테스트")
    class SendNotificationToUserTest {

        @Test
        @DisplayName("기기 토큰이 없으면 알림을 발송하지 않는다")
        void shouldNotSendWhenNoDeviceTokens() {
            // given
            User user = createAndSaveUser("userId1");

            // when & then - FirebaseMessaging을 호출하지 않으므로 예외 없이 종료
            fcmService.sendNotificationToUser(user, "제목", "본문");

            // 토큰이 없으므로 삭제할 것도 없음
            assertThat(fcmRepository.findByUser(user)).isEmpty();
            verifyNoInteractions(firebaseMessagingClient);
        }

        @Test
        @DisplayName("UNREGISTERED 에러가 발생하면 해당 토큰을 비활성화한다")
        void shouldDeactivateUnregisteredTokens() throws FirebaseMessagingException {
            // given
            User user = createAndSaveUser("userId3");
            String invalidToken = "invalid-fcm-token-unregistered";
            FCM_Token fcmToken = FCM_Token.create(user, invalidToken, "iPhone 15");
            fcmRepository.save(fcmToken);

            BatchResponse mockBatchResponse = mock(BatchResponse.class);
            SendResponse mockSendResponse = mock(SendResponse.class);
            FirebaseMessagingException mockException = mock(FirebaseMessagingException.class);

            given(mockBatchResponse.getSuccessCount()).willReturn(0);
            given(mockBatchResponse.getFailureCount()).willReturn(1);
            given(mockBatchResponse.getResponses()).willReturn(List.of(mockSendResponse));
            given(mockSendResponse.isSuccessful()).willReturn(false);
            given(mockSendResponse.getException()).willReturn(mockException);
            given(mockException.getMessagingErrorCode()).willReturn(MessagingErrorCode.UNREGISTERED);
            given(mockException.getMessage()).willReturn("Token is not registered");
            given(mockException.getHttpResponse()).willReturn(null);

            given(firebaseMessagingClient.sendEachForMulticast(any(MulticastMessage.class)))
                    .willReturn(mockBatchResponse);

            // when
            fcmService.sendNotificationToUser(user, "알림 제목", "알림 본문");

            // then - 이력은 유지하고 UNREGISTERED 토큰만 비활성화됨
            List<FCM_Token> tokens = fcmRepository.findByUser(user);
            assertThat(tokens).hasSize(1);
            assertThat(tokens.get(0).isActive()).isFalse();
            assertThat(tokens.get(0).getInvalidatedAt()).isNotNull();
        }

        @Test
        @DisplayName("이전 토큰의 UNREGISTERED 응답은 갱신된 토큰을 비활성화하지 않는다")
        void shouldNotDeactivateRefreshedTokenForStaleUnregisteredResponse()
                throws FirebaseMessagingException {
            // given
            User user = createAndSaveUser("userId-stale-unregistered");
            String attemptedToken = "old-fcm-token";
            String refreshedToken = "refreshed-fcm-token";
            FCM_Token fcmToken = fcmRepository.save(
                    FCM_Token.create(user, attemptedToken, "iPhone 15")
            );

            BatchResponse batchResponse = mock(BatchResponse.class);
            SendResponse sendResponse = mock(SendResponse.class);
            FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
            given(batchResponse.getResponses()).willReturn(List.of(sendResponse));
            given(sendResponse.isSuccessful()).willReturn(false);
            given(sendResponse.getException()).willReturn(exception);
            given(exception.getMessagingErrorCode()).willReturn(MessagingErrorCode.UNREGISTERED);

            given(firebaseMessagingClient.sendEachForMulticast(any(MulticastMessage.class)))
                    .willAnswer(invocation -> {
                        FCM_Token refreshed = fcmRepository.findById(fcmToken.getId()).orElseThrow();
                        refreshed.updateToken(refreshedToken);
                        fcmRepository.saveAndFlush(refreshed);
                        return batchResponse;
                    });

            // when
            fcmService.sendNotificationToUser(user, "알림 제목", "알림 본문");

            // then
            FCM_Token storedToken = fcmRepository.findById(fcmToken.getId()).orElseThrow();
            assertThat(storedToken.getFcmToken()).isEqualTo(refreshedToken);
            assertThat(storedToken.isActive()).isTrue();
            assertThat(storedToken.getInvalidatedAt()).isNull();
        }

        @Test
        @DisplayName("UNREGISTERED가 아닌 에러는 토큰을 비활성화하지 않는다")
        void shouldNotDeactivateTokensForOtherErrors() throws FirebaseMessagingException {
            // given
            User user = createAndSaveUser("userId4");
            String token = "fcm-token-internal-error";
            FCM_Token fcmToken = FCM_Token.create(user, token, "iPhone 15");
            fcmRepository.save(fcmToken);

            BatchResponse mockBatchResponse = mock(BatchResponse.class);
            SendResponse mockSendResponse = mock(SendResponse.class);
            FirebaseMessagingException mockException = mock(FirebaseMessagingException.class);

            given(mockBatchResponse.getSuccessCount()).willReturn(0);
            given(mockBatchResponse.getFailureCount()).willReturn(1);
            given(mockBatchResponse.getResponses()).willReturn(List.of(mockSendResponse));
            given(mockSendResponse.isSuccessful()).willReturn(false);
            given(mockSendResponse.getException()).willReturn(mockException);
            given(mockException.getMessagingErrorCode()).willReturn(MessagingErrorCode.INTERNAL);
            given(mockException.getMessage()).willReturn("Internal error");
            given(mockException.getHttpResponse()).willReturn(null);

            given(firebaseMessagingClient.sendEachForMulticast(any(MulticastMessage.class)))
                    .willReturn(mockBatchResponse);

            // when
            fcmService.sendNotificationToUser(user, "알림 제목", "알림 본문");

            // then - INTERNAL 에러는 토큰 삭제하지 않음
            List<FCM_Token> tokens = fcmRepository.findByUser(user);
            assertThat(tokens).hasSize(1);
            assertThat(tokens.get(0).isActive()).isTrue();
        }

        @Test
        @DisplayName("여러 기기에 알림을 발송하고 UNREGISTERED 토큰만 비활성화한다")
        void shouldDeactivateOnlyFailedUnregisteredTokens() throws FirebaseMessagingException {
            // given
            User user = createAndSaveUser("userId5");
            String validToken = "valid-fcm-token-multi";
            String invalidToken = "invalid-fcm-token-multi";
            FCM_Token validFcmToken = FCM_Token.create(user, validToken, "iPhone 15");
            FCM_Token invalidFcmToken = FCM_Token.create(user, invalidToken, "iPhone 15");
            fcmRepository.save(validFcmToken);
            fcmRepository.save(invalidFcmToken);

            BatchResponse mockBatchResponse = mock(BatchResponse.class);

            SendResponse successResponse = mock(SendResponse.class);
            given(successResponse.isSuccessful()).willReturn(true);
            given(successResponse.getMessageId()).willReturn("message-id-valid");

            SendResponse failResponse = mock(SendResponse.class);
            FirebaseMessagingException mockException = mock(FirebaseMessagingException.class);
            given(failResponse.isSuccessful()).willReturn(false);
            given(failResponse.getException()).willReturn(mockException);
            given(mockException.getMessagingErrorCode()).willReturn(MessagingErrorCode.UNREGISTERED);
            given(mockException.getMessage()).willReturn("Token is not registered");
            given(mockException.getHttpResponse()).willReturn(null);

            given(mockBatchResponse.getSuccessCount()).willReturn(1);
            given(mockBatchResponse.getFailureCount()).willReturn(1);
            given(mockBatchResponse.getResponses()).willReturn(List.of(successResponse, failResponse));

            given(firebaseMessagingClient.sendEachForMulticast(any(MulticastMessage.class)))
                    .willReturn(mockBatchResponse);

            // when
            fcmService.sendNotificationToUser(user, "알림 제목", "알림 본문");

            // then - 두 토큰의 이력은 모두 남고 만료 토큰만 비활성화됨
            List<FCM_Token> tokens = fcmRepository.findByUser(user);
            assertThat(tokens).hasSize(2);
            assertThat(tokens).filteredOn(token -> token.getFcmToken().equals(validToken))
                    .allMatch(FCM_Token::isActive);
            assertThat(tokens).filteredOn(token -> token.getFcmToken().equals(invalidToken))
                    .noneMatch(FCM_Token::isActive);
        }

        @Test
        @DisplayName("FirebaseMessagingException 발생 시 예외를 로깅하고 종료한다")
        void shouldHandleFirebaseMessagingException() throws FirebaseMessagingException {
            // given
            User user = createAndSaveUser("userId6");
            FCM_Token fcmToken = FCM_Token.create(user, "test-fcm-token-exception", "iPhone 15");
            fcmRepository.save(fcmToken);

            FirebaseMessagingException mockException = mock(FirebaseMessagingException.class);
            given(mockException.getMessage()).willReturn("FCM 서비스 에러");
            given(firebaseMessagingClient.sendEachForMulticast(any(MulticastMessage.class)))
                    .willThrow(mockException);

            // when & then - 예외가 발생하지 않고 정상 종료
            fcmService.sendNotificationToUser(user, "알림 제목", "알림 본문");

            // 토큰이 삭제되지 않음
            List<FCM_Token> tokens = fcmRepository.findByUser(user);
            assertThat(tokens).hasSize(1);
            assertThat(tokens.get(0).isActive()).isTrue();
        }

        @Test
        @DisplayName("활성 토큰이 없으면 Firebase를 호출하지 않는다")
        void shouldNotSendWhenNoActiveTokens() {
            // given
            User user = createAndSaveUser("userId7");
            FCM_Token inactiveToken = FCM_Token.create(user, "inactive-fcm-token", "iPhone 15");
            inactiveToken.deactivate();
            fcmRepository.save(inactiveToken);

            // when
            fcmService.sendNotificationToUser(user, "제목", "본문");

            // then
            verifyNoInteractions(firebaseMessagingClient);
            assertThat(fcmRepository.findByUser(user)).singleElement()
                    .matches(token -> !token.isActive());
        }
    }

    @Nested
    @DisplayName("sendMulticast 테스트")
    class SendMulticastTest {

        @Test
        @DisplayName("FCM 응답을 입력 대상 순서대로 성공·실패 결과에 매핑한다")
        void shouldMapResultsInTargetOrder() throws FirebaseMessagingException {
            // given
            SendResponse successResponse = mock(SendResponse.class);
            given(successResponse.isSuccessful()).willReturn(true);
            given(successResponse.getMessageId()).willReturn("message-id-1");

            FirebaseMessagingException retryableException = mock(FirebaseMessagingException.class);
            given(retryableException.getMessagingErrorCode()).willReturn(MessagingErrorCode.INTERNAL);
            IncomingHttpResponse httpResponse = mock(IncomingHttpResponse.class);
            given(httpResponse.getHeaders()).willReturn(Map.of("Retry-After", List.of("180")));
            given(retryableException.getHttpResponse()).willReturn(httpResponse);
            SendResponse retryableResponse = mock(SendResponse.class);
            given(retryableResponse.isSuccessful()).willReturn(false);
            given(retryableResponse.getException()).willReturn(retryableException);

            FirebaseMessagingException unregisteredException = mock(FirebaseMessagingException.class);
            given(unregisteredException.getMessagingErrorCode()).willReturn(MessagingErrorCode.UNREGISTERED);
            SendResponse unregisteredResponse = mock(SendResponse.class);
            given(unregisteredResponse.isSuccessful()).willReturn(false);
            given(unregisteredResponse.getException()).willReturn(unregisteredException);

            BatchResponse batchResponse = mock(BatchResponse.class);
            given(batchResponse.getResponses()).willReturn(List.of(
                    successResponse, retryableResponse, unregisteredResponse));
            given(firebaseMessagingClient.sendEachForMulticast(any(MulticastMessage.class)))
                    .willReturn(batchResponse);

            List<FcmTarget> targets = List.of(
                    new FcmTarget(11L, "token-1"),
                    new FcmTarget(22L, "token-2"),
                    new FcmTarget(33L, "token-3")
            );

            // when
            List<FcmSendResult> results = fcmService.sendMulticast(
                    "제목", "본문", EVENT_ID, targets);

            // then
            assertThat(results).containsExactly(
                    new FcmSendResult(
                            11L, "token-1", true, false, false, "message-id-1", null, null),
                    new FcmSendResult(
                            22L,
                            "token-2",
                            false,
                            true,
                            false,
                            null,
                            "INTERNAL",
                            Duration.ofMinutes(3)),
                    new FcmSendResult(
                            33L, "token-3", false, false, true, null, "UNREGISTERED", null)
            );
        }

        @Test
        @DisplayName("Multicast 전체 호출 예외를 모든 대상의 재시도 결과로 매핑한다")
        void shouldMapWholeCallExceptionToEveryTarget() throws FirebaseMessagingException {
            // given
            FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
            given(exception.getMessagingErrorCode()).willReturn(MessagingErrorCode.UNAVAILABLE);
            given(exception.getMessage()).willReturn("FCM unavailable");
            given(firebaseMessagingClient.sendEachForMulticast(any(MulticastMessage.class)))
                    .willThrow(exception);

            List<FcmTarget> targets = List.of(
                    new FcmTarget(41L, "token-4"),
                    new FcmTarget(42L, "token-5")
            );

            // when
            List<FcmSendResult> results = fcmService.sendMulticast(
                    "제목", "본문", EVENT_ID, targets);

            // then
            assertThat(results).containsExactly(
                    new FcmSendResult(
                            41L, "token-4", false, true, false, null, "UNAVAILABLE", null),
                    new FcmSendResult(
                            42L, "token-5", false, true, false, null, "UNAVAILABLE", null)
            );
        }

        @Test
        @DisplayName("FCM 응답이 발송 대상보다 짧으면 누락 건을 재시도 대상으로 반환한다")
        void shouldRetryMissingResponseEntries() throws FirebaseMessagingException {
            // given
            SendResponse successResponse = mock(SendResponse.class);
            given(successResponse.isSuccessful()).willReturn(true);
            given(successResponse.getMessageId()).willReturn("message-id-1");

            BatchResponse batchResponse = mock(BatchResponse.class);
            given(batchResponse.getResponses()).willReturn(List.of(successResponse));
            given(firebaseMessagingClient.sendEachForMulticast(any(MulticastMessage.class)))
                    .willReturn(batchResponse);

            List<FcmTarget> targets = List.of(
                    new FcmTarget(51L, "token-6"),
                    new FcmTarget(52L, "token-7")
            );

            // when
            List<FcmSendResult> results = fcmService.sendMulticast(
                    "제목", "본문", EVENT_ID, targets);

            // then
            assertThat(results).containsExactly(
                    new FcmSendResult(
                            51L, "token-6", true, false, false, "message-id-1", null, null),
                    new FcmSendResult(
                            52L,
                            "token-7",
                            false,
                            true,
                            false,
                            null,
                            "CLIENT_RESPONSE_MISMATCH",
                            null)
            );
        }

        @Test
        @DisplayName("메시지 구성 RuntimeException은 영구 실패로 반환한다")
        void shouldTreatMessageConstructionExceptionAsPermanentFailure() {
            // given
            List<FcmTarget> targets = List.of(new FcmTarget(61L, ""));

            // when
            List<FcmSendResult> results = fcmService.sendMulticast(
                    "제목", "본문", EVENT_ID, targets);

            // then
            assertThat(results).containsExactly(new FcmSendResult(
                    61L,
                    "",
                    false,
                    false,
                    false,
                    null,
                    "CLIENT_RUNTIME_EXCEPTION",
                    null
            ));
            verifyNoInteractions(firebaseMessagingClient);
        }
    }

    @Nested
    @DisplayName("updateFcmTokenV2 테스트")
    class UpdateFcmTokenV2Test {

        @Test
        @DisplayName("기존 토큰이 없으면 새로 생성한다")
        void shouldCreateNewTokenWhenEmpty() {
            // given
            String plainSecret = "test-device-secret-v2-new";
            User user = User.create(
                    "test-user-uuid-v2-new",
                    CryptoUtils.sha256(plainSecret),
                    CryptoUtils.sha256(plainSecret),
                    UserType.USER,
                    true,
                    plainSecret
            );
            userRepository.save(user);

            String newToken = "new-fcm-token-v2";
            String model = "iPhone 15 Pro";

            // when
            fcmService.updateFcmTokenV2(user.getUserId(), plainSecret, newToken, model);

            // then
            List<FCM_Token> tokens = fcmRepository.findByUser(user);
            assertThat(tokens).hasSize(1);
            assertThat(tokens.get(0).getFcmToken()).isEqualTo(newToken);
            assertThat(tokens.get(0).getModel()).isEqualTo(model);
        }

        @Test
        @DisplayName("서로 다른 사용자는 같은 토큰 문자열을 등록해도 DB 제약과 충돌하지 않는다")
        void shouldAllowSameTokenForDifferentUsers() {
            // given
            User firstUser = createAndSaveUser("token-owner-1");
            User secondUser = createAndSaveUser("token-owner-2");
            String sharedToken = "shared-fcm-token";

            // when
            fcmService.updateFcmTokenV2(
                    firstUser.getUserId(),
                    "plainSecret-token-owner-1",
                    sharedToken,
                    "iPhone 15"
            );
            fcmService.updateFcmTokenV2(
                    secondUser.getUserId(),
                    "plainSecret-token-owner-2",
                    sharedToken,
                    "iPhone 15"
            );

            // then
            assertThat(fcmRepository.findByUser(firstUser)).singleElement()
                    .extracting(FCM_Token::getFcmToken)
                    .isEqualTo(sharedToken);
            assertThat(fcmRepository.findByUser(secondUser)).singleElement()
                    .extracting(FCM_Token::getFcmToken)
                    .isEqualTo(sharedToken);
        }

        @Test
        @DisplayName("기존 토큰과 다르면 토큰을 업데이트한다")
        void shouldUpdateTokenWhenDifferent() {
            // given
            String plainSecret = "test-device-secret-v2-update";
            User user = User.create(
                    "test-user-uuid-v2-update",
                    CryptoUtils.sha256(plainSecret),
                    CryptoUtils.sha256(plainSecret),
                    UserType.USER,
                    true,
                    plainSecret
            );
            userRepository.save(user);

            String oldToken = "old-fcm-token-v2";
            String newToken = "new-fcm-token-v2";
            FCM_Token existingFcmToken = FCM_Token.create(user, oldToken, "iPhone 15");
            fcmRepository.save(existingFcmToken);

            // when
            fcmService.updateFcmTokenV2(user.getUserId(), plainSecret, newToken, "iPhone 15 Pro");

            // then
            List<FCM_Token> tokens = fcmRepository.findByUser(user);
            assertThat(tokens).hasSize(1);
            assertThat(tokens.get(0).getFcmToken()).isEqualTo(newToken);
        }

        @Test
        @DisplayName("기존 토큰과 같아도 비활성 토큰이면 다시 활성화한다")
        void shouldReactivateSameToken() {
            // given
            String plainSecret = "test-device-secret-v2-same";
            User user = User.create(
                    "test-user-uuid-v2-same",
                    CryptoUtils.sha256(plainSecret),
                    CryptoUtils.sha256(plainSecret),
                    UserType.USER,
                    true,
                    plainSecret
            );
            userRepository.save(user);

            String sameToken = "same-fcm-token-v2";
            FCM_Token existingFcmToken = FCM_Token.create(user, sameToken, "iPhone 15");
            existingFcmToken.deactivate();
            fcmRepository.save(existingFcmToken);

            // when
            fcmService.updateFcmTokenV2(user.getUserId(), plainSecret, sameToken, "iPhone 15");

            // then
            List<FCM_Token> tokens = fcmRepository.findByUser(user);
            assertThat(tokens).hasSize(1);
            assertThat(tokens.get(0).getFcmToken()).isEqualTo(sameToken);
            assertThat(tokens.get(0).isActive()).isTrue();
            assertThat(tokens.get(0).getInvalidatedAt()).isNull();
        }
    }

    private User createAndSaveUser(String userId) {
        String plainSecret = "plainSecret-" + userId;
        User user = User.create(
                userId,
                "hashedSecret",
                CryptoUtils.sha256(plainSecret),
                UserType.USER,
                true,
                plainSecret
        );
        return userRepository.save(user);
    }
}
