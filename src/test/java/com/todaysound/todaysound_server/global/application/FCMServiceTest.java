package com.todaysound.todaysound_server.global.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.google.firebase.messaging.BatchResponse;
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
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class FCMServiceTest extends ServiceTestSupport {

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
        }

        @Test
        @DisplayName("UNREGISTERED 에러가 발생하면 해당 토큰을 삭제한다")
        void shouldDeleteUnregisteredTokens() throws FirebaseMessagingException {
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

            // then - UNREGISTERED 토큰이 삭제됨
            assertThat(fcmRepository.findByUser(user)).isEmpty();
        }

        @Test
        @DisplayName("UNREGISTERED가 아닌 에러는 토큰을 삭제하지 않는다")
        void shouldNotDeleteTokensForOtherErrors() throws FirebaseMessagingException {
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
            assertThat(fcmRepository.findByUser(user)).hasSize(1);
        }

        @Test
        @DisplayName("여러 기기에 알림을 발송하고 일부 실패한 토큰만 삭제한다")
        void shouldDeleteOnlyFailedUnregisteredTokens() throws FirebaseMessagingException {
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

            // then - 유효한 토큰만 남음
            List<FCM_Token> remainingTokens = fcmRepository.findByUser(user);
            assertThat(remainingTokens).hasSize(1);
            assertThat(remainingTokens.get(0).getFcmToken()).isEqualTo(validToken);
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
            assertThat(fcmRepository.findByUser(user)).hasSize(1);
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
        @DisplayName("기존 토큰과 같으면 업데이트하지 않는다")
        void shouldNotUpdateTokenWhenSame() {
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
            fcmRepository.save(existingFcmToken);

            // when
            fcmService.updateFcmTokenV2(user.getUserId(), plainSecret, sameToken, "iPhone 15");

            // then
            List<FCM_Token> tokens = fcmRepository.findByUser(user);
            assertThat(tokens).hasSize(1);
            assertThat(tokens.get(0).getFcmToken()).isEqualTo(sameToken);
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
