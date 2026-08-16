ALTER TABLE payroll_records
    MODIFY COLUMN wage_earned DECIMAL(38, 4) NOT NULL,
    ADD COLUMN calculation_version INT NOT NULL DEFAULT 0 AFTER worked_seconds,
    ADD CONSTRAINT chk_payroll_records_calculation_version
        CHECK (calculation_version IN (0, 1));

-- Rows created before time-derived payroll lack a trustworthy hourly rate.
-- Preserve their wages as immutable version 0 snapshots.
UPDATE payroll_records
SET calculation_version = 1
WHERE hourly_rate > 0;

ALTER TABLE payroll_records
    MODIFY COLUMN calculation_version INT NOT NULL DEFAULT 1;
