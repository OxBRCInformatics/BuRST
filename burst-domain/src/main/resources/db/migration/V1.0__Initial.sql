-- Create report schema tables

CREATE SCHEMA IF NOT EXISTS report;

CREATE TABLE report.message
(
  id                BIGINT NOT NULL PRIMARY KEY,
  datetime_created  TIMESTAMP,
  datetime_received TIMESTAMP,
  message           VARCHAR(10485760),
  severity          VARCHAR(255),
  severity_number   INTEGER,
  source            VARCHAR(255),
  title             VARCHAR(255)
);
CREATE TABLE report.message_topics
(
  message_id BIGINT NOT NULL PRIMARY KEY,
  topics     VARCHAR(255)
);
CREATE TABLE report.metadata
(
  id      BIGINT NOT NULL PRIMARY KEY,
  key     VARCHAR(255),
  value   VARCHAR(255),
  message BIGINT
);
ALTER TABLE report.message_topics
  ADD FOREIGN KEY (message_id) REFERENCES report.message;
ALTER TABLE report.metadata
  ADD FOREIGN KEY (message) REFERENCES report.message;

-- Create subscriptions schema tables

CREATE SCHEMA IF NOT EXISTS subscription;

CREATE TABLE subscription.frequency (
  frequency VARCHAR(255) NOT NULL,
  PRIMARY KEY (frequency)
);
CREATE TABLE subscription.severity (
  severity VARCHAR(255) NOT NULL,
  PRIMARY KEY (severity)
);

CREATE TABLE subscription.subscription
(
  id                 BIGINT PRIMARY KEY NOT NULL,
  last_scheduled_run TIMESTAMP,
  next_scheduled_run TIMESTAMP,
  frequency          VARCHAR(255),
  severity           VARCHAR(255),
  subscriber_id      BIGINT,
  topics             VARCHAR(255)
);
CREATE TABLE subscription.users
(
  id            BIGINT PRIMARY KEY NOT NULL,
  email_address VARCHAR(255),
  first_name    VARCHAR(255),
  last_name     VARCHAR(255),
  organisation  VARCHAR(255)
);

ALTER TABLE subscription.subscription
  ADD FOREIGN KEY (frequency) REFERENCES subscription.frequency (frequency);
ALTER TABLE subscription.subscription
  ADD FOREIGN KEY (severity) REFERENCES subscription.severity (severity);
ALTER TABLE subscription.subscription
  ADD FOREIGN KEY (subscriber_id) REFERENCES subscription.users (id);
CREATE UNIQUE INDEX unique_email_address ON subscription.users (email_address);

CREATE SEQUENCE subscription.subscription_id_seq MINVALUE 1 START 1 CACHE 50 OWNED BY subscription.subscription.id;
CREATE SEQUENCE subscription.users_id_seq MINVALUE 1 START 1 CACHE 50 OWNED BY subscription.users.id;

-- Load enumeration data

INSERT INTO Subscription.Frequency (frequency) VALUES ('IMMEDIATE');
INSERT INTO Subscription.Frequency (frequency) VALUES ('DAILY');
INSERT INTO Subscription.Frequency (frequency) VALUES ('WEEKLY');
INSERT INTO Subscription.Frequency (frequency) VALUES ('MONTHLY');

INSERT INTO Subscription.Severity (severity) VALUES ('DEBUG');
INSERT INTO Subscription.Severity (severity) VALUES ('INFORMATIONAL');
INSERT INTO Subscription.Severity (severity) VALUES ('NOTICE');
INSERT INTO Subscription.Severity (severity) VALUES ('WARNING');
INSERT INTO Subscription.Severity (severity) VALUES ('ERROR');
INSERT INTO Subscription.Severity (severity) VALUES ('CRITICAL');
INSERT INTO Subscription.Severity (severity) VALUES ('ALERT');
INSERT INTO Subscription.Severity (severity) VALUES ('EMERGENCY');

