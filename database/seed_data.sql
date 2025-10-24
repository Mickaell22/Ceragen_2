-- Initial seed data for Ceragen System
-- Run after schema.sql

-- Insert default admin user
-- username: admin
-- password: admin (hashed with bcrypt)
INSERT INTO usuarios (username, password, rol, activo) VALUES
('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYvAkHDjP4G', 'ADMIN', TRUE);

-- Insert sample especialidades
INSERT INTO especialidades (nombre, descripcion, costo_consulta) VALUES
('Medicina General', 'Consultas médicas generales', 25.00),
('Pediatría', 'Atención especializada para niños', 30.00),
('Cardiología', 'Especialidad en el sistema cardiovascular', 50.00),
('Ginecología', 'Salud de la mujer', 40.00),
('Traumatología', 'Lesiones del sistema músculo-esquelético', 45.00);

-- Note: Add more sample data as needed
-- To generate bcrypt hashes: https://bcrypt-generator.com/ (use cost factor 12)
