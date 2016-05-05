DO
$do$
BEGIN
  IF exists(SELECT schema_name
            FROM INFORMATION_SCHEMA.SCHEMATA
            WHERE schema_name = 'report')
  THEN
    ALTER SCHEMA report
    RENAME TO old_report;
  END IF;
END
$do$;

DO
$do$
BEGIN
  IF exists(SELECT schema_name
            FROM INFORMATION_SCHEMA.SCHEMATA
            WHERE schema_name = 'subscription')
  THEN
    ALTER SCHEMA subscription
    RENAME TO old_subscription;
  END IF;
END
$do$;