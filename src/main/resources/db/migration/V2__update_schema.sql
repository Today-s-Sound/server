
-- summaries: is_read 삭제, is_keyword_matched 추가
ALTER TABLE summaries DROP COLUMN is_read;
ALTER TABLE summaries ADD COLUMN is_keyword_matched BIT NOT NULL DEFAULT FALSE;

-- subscriptions: is_urgent 삭제
ALTER TABLE subscriptions DROP COLUMN is_urgent;