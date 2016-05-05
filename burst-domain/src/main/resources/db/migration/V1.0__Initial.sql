--
-- Create schemas
--

CREATE SCHEMA report;
CREATE SCHEMA subscription;

--
-- Create report tables
--
SET search_path = report, pg_catalog;

CREATE TABLE message (
  id                BIGINT NOT NULL PRIMARY KEY,
  datetime_created  TIMESTAMP WITHOUT TIME ZONE,
  datetime_received TIMESTAMP WITHOUT TIME ZONE,
  message           TEXT,
  severity          CHARACTER VARYING(255),
  severity_number   INTEGER,
  source            CHARACTER VARYING(255),
  title             CHARACTER VARYING(255)
);

CREATE TABLE message_topics (
  message_id BIGINT NOT NULL,
  topics     CHARACTER VARYING(255)
);

CREATE SEQUENCE messages_id_seq
START WITH 1
INCREMENT BY 1
NO MINVALUE
NO MAXVALUE
CACHE 1
OWNED BY report.message.id;
;

CREATE TABLE metadata (
  id      BIGINT NOT NULL,
  key     CHARACTER VARYING(255),
  value   CHARACTER VARYING(255),
  message BIGINT
);

CREATE SEQUENCE metadata_id_seq
START WITH 1
INCREMENT BY 1
NO MINVALUE
NO MAXVALUE
CACHE 1
OWNED BY report.metadata.id;

--
-- Create subscription tables
--

SET search_path = subscription, pg_catalog;

CREATE TABLE frequency (
  frequency CHARACTER VARYING(32) NOT NULL PRIMARY KEY
);

CREATE TABLE severity (
  severity CHARACTER VARYING(32) NOT NULL PRIMARY KEY
);

CREATE TABLE subscription (
  id                       BIGINT NOT NULL PRIMARY KEY,
  last_scheduled_run       TIMESTAMP WITHOUT TIME ZONE,
  next_scheduled_run       TIMESTAMP WITHOUT TIME ZONE,
  topics                   TEXT,
  frequency                CHARACTER VARYING(32),
  severity                 CHARACTER VARYING(32),
  subscriber_email_address CHARACTER VARYING(255)
);

CREATE TABLE subscription_topics (
  subscription_id BIGINT NOT NULL,
  topics          CHARACTER VARYING(255)
);

CREATE TABLE users (
  email_address CHARACTER VARYING(255) NOT NULL PRIMARY KEY,
  first_name    CHARACTER VARYING(255),
  last_name     CHARACTER VARYING(255),
  organisation  CHARACTER VARYING(255)
);

CREATE SEQUENCE subscription_id_seq
START WITH 1
INCREMENT BY 1
NO MINVALUE
NO MAXVALUE
CACHE 1
OWNED BY subscription.subscription.id;

--
-- Create report constraints
--

SET search_path = report, pg_catalog;

SELECT pg_catalog.setval('messages_id_seq', 1, FALSE);
SELECT pg_catalog.setval('metadata_id_seq', 1, FALSE);

ALTER TABLE ONLY message_topics
  ADD CONSTRAINT fk_topics_messages FOREIGN KEY (message_id) REFERENCES message (id);
ALTER TABLE ONLY metadata
  ADD CONSTRAINT fk_metadata_messages FOREIGN KEY (message) REFERENCES message (id);

ALTER TABLE ONLY message
  ALTER COLUMN id SET DEFAULT nextval('messages_id_seq' :: REGCLASS);
ALTER TABLE ONLY metadata
  ALTER COLUMN id SET DEFAULT nextval('metadata_id_seq' :: REGCLASS);

--
-- Create subscription constraints
--

SET search_path = subscription, pg_catalog;

SELECT pg_catalog.setval('subscription_id_seq', 1, FALSE);

ALTER TABLE ONLY subscription
  ADD CONSTRAINT fk_subscription_users FOREIGN KEY (subscriber_email_address) REFERENCES users (email_address);
ALTER TABLE ONLY subscription
  ADD CONSTRAINT fk_subscription_frequency FOREIGN KEY (frequency) REFERENCES frequency (frequency);
ALTER TABLE ONLY subscription
  ADD CONSTRAINT fk_subscription_severity FOREIGN KEY (severity) REFERENCES severity (severity);
ALTER TABLE ONLY subscription_topics
  ADD CONSTRAINT fk_topics_subscription FOREIGN KEY (subscription_id) REFERENCES subscription (id);

ALTER TABLE ONLY subscription
  ALTER COLUMN id SET DEFAULT nextval('subscription_id_seq' :: REGCLASS);

--
-- Load enumeration data
--

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

