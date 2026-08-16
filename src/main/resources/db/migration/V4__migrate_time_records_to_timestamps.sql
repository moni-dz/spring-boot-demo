ALTER TABLE time_records
    DROP CHECK chk_time_records_interval,
    ADD COLUMN time_in TIMESTAMP(6) NULL AFTER employee_id,
    ADD COLUMN time_out TIMESTAMP(6) NULL AFTER time_in;

ALTER TABLE time_records
    ADD INDEX idx_time_records_employee_timestamp_interval (employee_id, time_out, time_in);

ALTER TABLE time_records
    DROP INDEX idx_time_records_employee_interval;

SET @time_record_previous_time_zone = @@SESSION.time_zone;
SET SESSION time_zone = '+00:00';

UPDATE time_records
SET time_in = FROM_UNIXTIME(time_in_epoch),
    time_out = CASE
        WHEN time_out_epoch IS NULL THEN NULL
        ELSE FROM_UNIXTIME(time_out_epoch)
    END;

SET SESSION time_zone = @time_record_previous_time_zone;

ALTER TABLE time_records
    MODIFY COLUMN time_in TIMESTAMP(6) NOT NULL,
    ADD CONSTRAINT chk_time_records_interval
        CHECK (time_out IS NULL OR time_in <= time_out),
    DROP COLUMN time_in_epoch,
    DROP COLUMN time_out_epoch;
