ALTER TABLE payroll_records
    ADD COLUMN hourly_rate DECIMAL(19, 4) NOT NULL DEFAULT 0 AFTER interval_end,
    ADD COLUMN worked_seconds BIGINT UNSIGNED NOT NULL DEFAULT 0 AFTER hourly_rate,
    ADD CONSTRAINT chk_payroll_records_hourly_rate CHECK (hourly_rate >= 0);
