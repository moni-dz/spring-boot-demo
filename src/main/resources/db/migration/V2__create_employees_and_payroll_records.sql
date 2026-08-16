CREATE TABLE employees (
    id BIGINT NOT NULL AUTO_INCREMENT,
    last_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT chk_employees_last_name_not_blank CHECK (CHAR_LENGTH(TRIM(last_name)) > 0),
    CONSTRAINT chk_employees_first_name_not_blank CHECK (CHAR_LENGTH(TRIM(first_name)) > 0)
);

ALTER TABLE time_records
    ADD COLUMN employee_id BIGINT NOT NULL AFTER id,
    ADD CONSTRAINT fk_time_records_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE RESTRICT,
    ADD INDEX idx_time_records_employee_interval (employee_id, time_out_epoch, time_in_epoch),
    ADD CONSTRAINT chk_time_records_interval
        CHECK (time_in_epoch IS NOT NULL AND (time_out_epoch IS NULL OR time_in_epoch <= time_out_epoch)),
    DROP COLUMN name;

CREATE TABLE payroll_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    interval_start TIMESTAMP(6) NOT NULL,
    interval_end TIMESTAMP(6) NOT NULL,
    hourly_rate DECIMAL(19, 4) NOT NULL DEFAULT 0,
    worked_seconds BIGINT UNSIGNED NOT NULL DEFAULT 0,
    calculation_version INT NOT NULL DEFAULT 1,
    wage_earned DECIMAL(38, 4) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_payroll_records_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE RESTRICT,
    CONSTRAINT uq_payroll_records_employee_interval
        UNIQUE (employee_id, interval_start, interval_end),
    CONSTRAINT chk_payroll_records_interval CHECK (interval_start < interval_end),
    CONSTRAINT chk_payroll_records_hourly_rate CHECK (hourly_rate >= 0),
    CONSTRAINT chk_payroll_records_calculation_version CHECK (calculation_version IN (0, 1)),
    CONSTRAINT chk_payroll_records_wage CHECK (wage_earned >= 0),
    CONSTRAINT chk_payroll_records_deleted_at CHECK (deleted_at IS NULL OR deleted_at >= created_at),
    INDEX idx_payroll_records_stale (employee_id, deleted_at, interval_end)
);
