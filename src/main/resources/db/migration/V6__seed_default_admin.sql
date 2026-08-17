INSERT INTO employees (last_name, first_name, username, email, password_hash)
VALUES ('Admin', 'Default', 'admin', 'admin@example.com', '$2b$10$Mln/qLRVtQM9nI06opkLMOfS4c2OCExbO/iZALyIrttkGFre..Umi');

INSERT INTO employee_roles (employee_id, role_id)
SELECT e.id, r.id FROM employees e, roles r
WHERE e.username = 'admin' AND r.name = 'ADMIN';
