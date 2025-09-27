package com.todaysound.todaysound_server.domain.user.entity;

import com.todaysound.todaysound_server.domain.subscription.entity.Subscription;
import com.todaysound.todaysound_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends BaseEntity {

    //********************************* static final 상수 필드 *********************************/

    /**
     * 비밀번호 해시화를 위한 BCrypt 인코더
     */
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /********************************* PK 필드 *********************************/

    /**
     * 기본 키는 BaseEntity에서 상속받음
     * @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
     * private Long id;
     */

    /********************************* PK가 아닌 필드 *********************************/

    /**
     * 사용자 고유 식별자 (UUID)
     * API 응답에서 사용되는 외부 식별자
     */
    @Column(name = "user_id", unique = true, nullable = false, length = 36)
    private String userId;

    /**
     * AUTH에 필요한 필드 - 해시화된 디바이스 시크릿
     * 보안을 위해 평문 저장 금지, BCrypt로 해시화
     */
    @Column(name = "hashed_secret", nullable = false, length = 255)
    private String hashedSecret;

    /**
     * 중복 검사용 지문 (고정 출력 해시: 예, SHA-256)
     * - BCrypt는 솔트로 인해 매번 값이 달라 중복 비교에 부적합
     * - fingerprint는 유니크 인덱스로 중복을 방지하는 용도로 사용
     */
    @Column(name = "secret_fingerprint", nullable = false, unique = true, length = 64)
    private String secretFingerprint;

    /**
     * 사용자 타입 (익명/등록/관리자)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    /**
     * 사용자 활성 상태
     * 기본값 true, 탈퇴 시 false로 변경
     */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /********************************* 비영속 필드 *********************************/

    /**
     * 평문 시크릿 (비영속 필드)
     * @Transient로 JPA가 DB에 저장하지 않도록 설정
     * 생성 시에만 사용하고 저장 후에는 null 처리
     */
    @Transient
    private String plainSecret;


    /********************************* 연관관계 매핑 *********************************/

    /**
     * 사용자의 구독 목록
     * @OneToMany: 1:N 관계, User 1개가 여러 Subscription을 가질 수 있음
     * mappedBy = "user": Subscription 엔티티의 user 필드가 연관관계의 주인
     * cascade = CascadeType.ALL: User 삭제 시 관련 Subscription도 함께 삭제
     * orphanRemoval = true: 고아 객체(연관관계가 끊어진 객체) 자동 삭제
     * fetch = FetchType.LAZY: 지연 로딩으로 성능 최적화
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Subscription> subscriptions = new ArrayList<>();

    /********************************* 생성 메서드 *********************************/

    /********************************* 비니지스 로직 *********************************/

    /**
     * 시크릿 검증 메서드
     * @param inputSecret 입력받은 평문 시크릿
     * @return 검증 결과
     */
    public boolean verifySecret(String inputSecret) {
        return PASSWORD_ENCODER.matches(inputSecret, this.hashedSecret);
    }

    /**
     * 사용자 비활성화
     * 소프트 삭제 패턴 적용
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 사용자 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 익명 사용자 여부 확인
     */
    public boolean isAnonymous() {
        return UserType.ANONYMOUS.equals(this.userType);
    }

    /**
     * 등록된 사용자 여부 확인
     */
    public boolean isRegistered() {
        return UserType.USER.equals(this.userType);
    }

    /**
     * 관리자 여부 확인
     */
    public boolean isAdmin() {
        return UserType.ADMIN.equals(this.userType);
    }

    /**
     * 평문 시크릿 제거 (보안)
     * 생성 후 메모리에서 평문 제거
     */
    public void clearPlainSecret() {
        this.plainSecret = null;
    }
}
