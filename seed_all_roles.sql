USE vehicle_booking;

-- First, clear existing users to avoid UUID conflicts if re-running
DELETE FROM users;

-- Insert all required roles with password: password123
-- Password hash: $2a$10$W0E4AHUFHAFnQ4fpxBTaiu5ZQKsLlCQmbt9Mv1WIDoRRyulqfbwVu

INSERT INTO users (id, email, first_name, last_name, password, role, is_active, must_change_password) VALUES 
(UUID_TO_BIN(UUID()), 'admin@alteram.co.za', 'System', 'Admin', '$2a$10$W0E4AHUFHAFnQ4fpxBTaiu5ZQKsLlCQmbt9Mv1WIDoRRyulqfbwVu', 'ADMIN', 1, 0),
(UUID_TO_BIN(UUID()), 'employee@alteram.co.za', 'Test', 'Employee', '$2a$10$W0E4AHUFHAFnQ4fpxBTaiu5ZQKsLlCQmbt9Mv1WIDoRRyulqfbwVu', 'EMPLOYEE', 1, 0),
(UUID_TO_BIN(UUID()), 'fleet@alteram.co.za', 'Test', 'Fleet', '$2a$10$W0E4AHUFHAFnQ4fpxBTaiu5ZQKsLlCQmbt9Mv1WIDoRRyulqfbwVu', 'FLEET_MANAGER', 1, 0),
(UUID_TO_BIN(UUID()), 'manager@alteram.co.za', 'Test', 'Manager', '$2a$10$W0E4AHUFHAFnQ4fpxBTaiu5ZQKsLlCQmbt9Mv1WIDoRRyulqfbwVu', 'LINE_MANAGER', 1, 0),
(UUID_TO_BIN(UUID()), 'ops@alteram.co.za', 'Test', 'Ops', '$2a$10$W0E4AHUFHAFnQ4fpxBTaiu5ZQKsLlCQmbt9Mv1WIDoRRyulqfbwVu', 'OPS_OFFICIAL', 1, 0),
(UUID_TO_BIN(UUID()), 'security@alteram.co.za', 'Test', 'Security', '$2a$10$W0E4AHUFHAFnQ4fpxBTaiu5ZQKsLlCQmbt9Mv1WIDoRRyulqfbwVu', 'SECURITY', 1, 0);

-- Ensure some vehicles exist for testing
DELETE FROM vehicles;
INSERT INTO vehicles (plate, make, model, status) VALUES 
('ABC123GP', 'Toyota', 'Corolla', 'AVAILABLE'),
('XYZ789GP', 'VW', 'Polo', 'AVAILABLE'),
('KRT456GP', 'Ford', 'Ranger', 'AVAILABLE'),
('LMN001GP', 'Mercedes', 'Sprinter', 'AVAILABLE');
