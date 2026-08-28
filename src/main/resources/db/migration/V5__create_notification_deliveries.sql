ALTER TABLE fcm_tokens
    ADD COLUMN is_active BIT NOT NULL DEFAULT TRUE,
    ADD COLUMN invalidated_at DATETIME(6) NULL,
    ADD INDEX idx_fcm_tokens_user_active (user_id, is_active);

ALTER TABLE summaries
    ADD CONSTRAINT uk_summaries_subscription_site_post UNIQUE (subscription_id, hash_tag);

CREATE TABLE notification_deliveries
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    summary_id       BIGINT       NOT NULL,
    fcm_token_id     BIGINT       NOT NULL,
    event_id         CHAR(64)     NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    attempt_count    INT          NOT NULL,
    next_attempt_at  DATETIME(6)  NULL,
    lease_until      DATETIME(6)  NULL,
    last_error_code  VARCHAR(64)  NULL,
    fcm_message_id   VARCHAR(255) NULL,
    created_at       DATETIME(6)  NOT NULL,
    sent_at          DATETIME(6)  NULL,
    CONSTRAINT fk_notification_deliveries_summary
        FOREIGN KEY (summary_id) REFERENCES summaries (id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_deliveries_fcm_token
        FOREIGN KEY (fcm_token_id) REFERENCES fcm_tokens (id) ON DELETE CASCADE,
    CONSTRAINT uk_notification_deliveries_summary_token
        UNIQUE (summary_id, fcm_token_id),
    CONSTRAINT uk_notification_deliveries_event_token
        UNIQUE (event_id, fcm_token_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_notification_deliveries_status_next_attempt
    ON notification_deliveries (status, next_attempt_at, id);

CREATE INDEX idx_notification_deliveries_status_lease
    ON notification_deliveries (status, lease_until, id);

CREATE INDEX idx_notification_deliveries_fcm_token
    ON notification_deliveries (fcm_token_id);
