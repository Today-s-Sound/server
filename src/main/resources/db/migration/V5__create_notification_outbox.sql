-- notification_outbox: Transactional Outbox 테이블 생성
CREATE TABLE notification_outbox
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    title       VARCHAR(255)  NOT NULL,
    body        TEXT          NOT NULL,
    status      VARCHAR(20)   NOT NULL,
    retry_count INT           NOT NULL,
    created_at  DATETIME(6)   NOT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 조회 최적화를 위한 인덱스 (PENDING + created_at 순)
CREATE INDEX idx_notification_outbox_status_created_at
    ON notification_outbox (status, created_at);