CREATE INDEX index_dr_s
  ON report.message (datetime_received, severity_number);

CREATE INDEX index_nsr
  ON subscription.subscription (next_scheduled_run);

CREATE INDEX index_nsr_lsr_nsr
  ON subscription.subscription (next_scheduled_run, last_scheduled_run, next_scheduled_run);