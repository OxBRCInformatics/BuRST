ALTER TABLE report.message_topics
  RENAME COLUMN topics TO topic;

CREATE INDEX index_topic
  ON report.message_topics (topic);

ALTER TABLE report.metadata
  RENAME COLUMN message TO message_id;

CREATE INDEX index_key
  ON report.metadata (key);

CREATE INDEX index_datetime_received
  ON report.message (datetime_received);
CREATE INDEX index_severity_number
  ON report.message (severity_number);


ALTER TABLE subscription.subscription
  RENAME COLUMN severity TO severity_id;

ALTER TABLE subscription.subscription
  RENAME COLUMN frequency TO frequency_id;

CREATE INDEX index_lsr_si
  ON subscription.subscription (last_scheduled_run, severity_id);
CREATE INDEX index_nsr_lsr
  ON subscription.subscription (next_scheduled_run, last_scheduled_run);

