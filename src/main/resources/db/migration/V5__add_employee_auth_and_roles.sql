CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE (name)
);

INSERT INTO roles (name) VALUES ('ADMIN'), ('USER');

ALTER TABLE employees
    ADD COLUMN username VARCHAR(100) NOT NULL AFTER id,
    ADD COLUMN email VARCHAR(255) NOT NULL AFTER username,
    ADD COLUMN password_hash VARCHAR(255) NOT NULL AFTER email,
    ADD CONSTRAINT uq_employees_username UNIQUE (username),
    ADD CONSTRAINT uq_employees_email UNIQUE (email);

CREATE TABLE employee_roles (
    employee_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (employee_id, role_id),
    CONSTRAINT fk_employee_roles_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE,
    CONSTRAINT fk_employee_roles_role
        FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);
