ALTER TABLE time_records
    ADD COLUMN employee_id BIGINT NULL AFTER id;

-- Legacy free-form names cannot be split safely. Preserve each value as a
-- migration-only employee marker, then remove the marker after assigning FKs.
ALTER TABLE employees
    ADD COLUMN legacy_time_record_name VARCHAR(1000) NULL;

INSERT INTO employees (last_name, first_name, middle_name, legacy_time_record_name)
SELECT
    'Legacy',
    LEFT(COALESCE(NULLIF(TRIM(name), ''), 'Unknown'), 100),
    NULL,
    name
FROM time_records
GROUP BY name;

UPDATE time_records record
JOIN employees employee ON employee.legacy_time_record_name = record.name
SET record.employee_id = employee.id;

ALTER TABLE time_records
    MODIFY employee_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_time_records_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE RESTRICT,
    ADD INDEX idx_time_records_employee_interval (employee_id, time_out_epoch, time_in_epoch),
    ADD CONSTRAINT chk_time_records_interval
        CHECK (time_in_epoch IS NOT NULL AND (time_out_epoch IS NULL OR time_in_epoch <= time_out_epoch)),
    DROP COLUMN name;

ALTER TABLE employees
    DROP COLUMN legacy_time_record_name;
