CREATE TABLE employees (
    id BIGINT NOT NULL AUTO_INCREMENT,
    last_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT chk_employees_last_name_not_blank CHECK (CHAR_LENGTH(TRIM(last_name)) > 0),
    CONSTRAINT chk_employees_first_name_not_blank CHECK (CHAR_LENGTH(TRIM(first_name)) > 0)
);

CREATE TABLE payroll_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    interval_start TIMESTAMP(6) NOT NULL,
    interval_end TIMESTAMP(6) NOT NULL,
    wage_earned DECIMAL(19, 4) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    deleted_at TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_payroll_records_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE RESTRICT,
    CONSTRAINT uq_payroll_records_employee_interval
        UNIQUE (employee_id, interval_start, interval_end),
    CONSTRAINT chk_payroll_records_interval CHECK (interval_start < interval_end),
    CONSTRAINT chk_payroll_records_wage CHECK (wage_earned >= 0),
    CONSTRAINT chk_payroll_records_deleted_at CHECK (deleted_at IS NULL OR deleted_at >= created_at),
    INDEX idx_payroll_records_stale (employee_id, deleted_at, interval_end)
);
