INSERT INTO users (id, email, first_name, last_name, password, role, is_active, must_change_password) VALUES 
(UUID_TO_BIN(UUID()), 'admin@alteram.co.za', 'System', 'Admin', '$2a$10$5YlcT3PFDU1KxDi7H7RRle0.Y1uMA/.L.co7/ajrgpgi5VkUICoCm', 'ADMIN', 1, 0),
(UUID_TO_BIN(UUID()), 'employee@alteram.co.za', 'Test', 'Employee', '$2a$10$5YlcT3PFDU1KxDi7H7RRle0.Y1uMA/.L.co7/ajrgpgi5VkUICoCm', 'EMPLOYEE', 1, 0),
(UUID_TO_BIN(UUID()), 'fleet@alteram.co.za', 'Test', 'Fleet', '$2a$10$5YlcT3PFDU1KxDi7H7RRle0.Y1uMA/.L.co7/ajrgpgi5VkUICoCm', 'FLEET_MANAGER', 1, 0),
(UUID_TO_BIN(UUID()), 'security@alteram.co.za', 'Test', 'Security', '$2a$10$5YlcT3PFDU1KxDi7H7RRle0.Y1uMA/.L.co7/ajrgpgi5VkUICoCm', 'SECURITY', 1, 0),
(UUID_TO_BIN(UUID()), 'ops@alteram.co.za', 'Test', 'Ops', '$2a$10$5YlcT3PFDU1KxDi7H7RRle0.Y1uMA/.L.co7/ajrgpgi5VkUICoCm', 'OPS_OFFICIAL', 1, 0),
(UUID_TO_BIN(UUID()), 'manager@alteram.co.za', 'Test', 'Manager', '$2a$10$5YlcT3PFDU1KxDi7H7RRle0.Y1uMA/.L.co7/ajrgpgi5VkUICoCm', 'LINE_MANAGER', 1, 0);
INSERT INTO vehicles (plate, make, model, status) VALUES 
('ABC123GP', 'Toyota', 'Corolla', 'AVAILABLE'),
('XYZ789GP', 'VW', 'Polo', 'AVAILABLE'),
('KRT456GP', 'Ford', 'Ranger', 'AVAILABLE');
