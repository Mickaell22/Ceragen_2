-- Initial seed data for Ceragen System
-- Run after schema.sql
-- Password for all users: admin123

USE railway;

-- Insert usuarios (password: admin123 - bcrypt hashed with cost factor 12)
INSERT INTO usuarios (username, password, rol, activo) VALUES
('admin', '$2a$12$UbYLl8XrHgWvEXGbJXaa2.9egRu5oFkCpU/w3Y0xA0Qp1ZrTlpyLq', 'ADMIN', TRUE),
('recepcion1', '$2a$12$UbYLl8XrHgWvEXGbJXaa2.9egRu5oFkCpU/w3Y0xA0Qp1ZrTlpyLq', 'RECEPCIONISTA', TRUE),
('recepcion2', '$2a$12$UbYLl8XrHgWvEXGbJXaa2.9egRu5oFkCpU/w3Y0xA0Qp1ZrTlpyLq', 'RECEPCIONISTA', TRUE),
('dr_mendoza', '$2a$12$UbYLl8XrHgWvEXGbJXaa2.9egRu5oFkCpU/w3Y0xA0Qp1ZrTlpyLq', 'MEDICO', TRUE),
('dra_rodriguez', '$2a$12$UbYLl8XrHgWvEXGbJXaa2.9egRu5oFkCpU/w3Y0xA0Qp1ZrTlpyLq', 'MEDICO', TRUE),
('dr_sanchez', '$2a$12$UbYLl8XrHgWvEXGbJXaa2.9egRu5oFkCpU/w3Y0xA0Qp1ZrTlpyLq', 'MEDICO', TRUE),
('dra_gomez', '$2a$12$UbYLl8XrHgWvEXGbJXaa2.9egRu5oFkCpU/w3Y0xA0Qp1ZrTlpyLq', 'MEDICO', TRUE),
('dr_martinez', '$2a$12$UbYLl8XrHgWvEXGbJXaa2.9egRu5oFkCpU/w3Y0xA0Qp1ZrTlpyLq', 'MEDICO', TRUE);

-- Insert especialidades
INSERT INTO especialidades (nombre, descripcion, costo_consulta) VALUES
('Medicina General', 'Atención primaria de salud para pacientes de todas las edades', 50.00),
('Pediatría', 'Especialidad médica dedicada al cuidado de bebés, niños y adolescentes', 60.00),
('Cardiología', 'Diagnóstico y tratamiento de enfermedades del corazón y sistema circulatorio', 80.00),
('Dermatología', 'Tratamiento de enfermedades de la piel, cabello y uñas', 65.00),
('Ginecología', 'Salud del sistema reproductor femenino', 70.00),
('Traumatología', 'Tratamiento de lesiones del sistema músculo-esquelético', 75.00),
('Oftalmología', 'Diagnóstico y tratamiento de enfermedades de los ojos', 55.00),
('Psiquiatría', 'Diagnóstico y tratamiento de trastornos mentales', 90.00),
('Nutrición', 'Asesoramiento nutricional y dietas terapéuticas', 45.00),
('Odontología', 'Diagnóstico, prevención y tratamiento de enfermedades dentales', 40.00);

-- Insert profesionales
INSERT INTO profesionales (cedula, nombres, apellidos, especialidad_id, telefono, email, numero_licencia, activo, usuario_id) VALUES
('0912345678', 'Carlos', 'Mendoza García', 1, '0987654321', 'cmendoza@ceragen.com', 'MED-2020-001', TRUE, 4),
('0923456789', 'María', 'Rodríguez López', 2, '0987654322', 'mrodriguez@ceragen.com', 'MED-2019-045', TRUE, 5),
('0934567890', 'Jorge', 'Sánchez Pérez', 3, '0987654323', 'jsanchez@ceragen.com', 'MED-2018-089', TRUE, 6),
('0945678901', 'Ana', 'Gómez Torres', 4, '0987654324', 'agomez@ceragen.com', 'MED-2021-023', TRUE, 7),
('0956789012', 'Luis', 'Martínez Silva', 5, '0987654325', 'lmartinez@ceragen.com', 'MED-2017-112', TRUE, 8),
('0967890123', 'Patricia', 'Flores Ruiz', 6, '0987654326', 'pflores@ceragen.com', 'MED-2020-067', TRUE, NULL),
('0978901234', 'Roberto', 'Castro Vargas', 7, '0987654327', 'rcastro@ceragen.com', 'MED-2019-134', TRUE, NULL),
('0989012345', 'Carmen', 'Vega Morales', 8, '0987654328', 'cvega@ceragen.com', 'MED-2018-178', TRUE, NULL),
('0990123456', 'Diego', 'Herrera Guzmán', 9, '0987654329', 'dherrera@ceragen.com', 'NUT-2021-045', TRUE, NULL),
('0901234567', 'Sofía', 'Ramírez Ortiz', 10, '0987654330', 'sramirez@ceragen.com', 'ODO-2020-089', TRUE, NULL);

-- Insert pacientes
INSERT INTO pacientes (cedula, nombres, apellidos, fecha_nacimiento, genero, telefono, email, direccion, grupo_sanguineo, alergias) VALUES
('0912345001', 'Juan', 'Pérez Gómez', '1985-03-15', 'M', '0991234567', 'jperez@email.com', 'Av. 9 de Octubre y Malecón, Guayaquil', 'O+', 'Penicilina'),
('0923456002', 'Laura', 'García Morales', '1990-07-22', 'F', '0991234568', 'lgarcia@email.com', 'Cdla. Kennedy Norte, Mz. 12, Guayaquil', 'A+', 'Ninguna'),
('0934567003', 'Pedro', 'López Silva', '1978-11-30', 'M', '0991234569', 'plopez@email.com', 'Urdesa Central, Calle 3ra, Guayaquil', 'B+', 'Polen, Mariscos'),
('0945678004', 'Carmen', 'Díaz Torres', '1995-01-18', 'F', '0991234570', 'cdiaz@email.com', 'Sauces 4, Mz. 345, Guayaquil', 'AB+', 'Ninguna'),
('0956789005', 'Roberto', 'Martínez Ruiz', '1982-05-25', 'M', '0991234571', 'rmartinez@email.com', 'Ceibos, Etapa 2, Mz. 56, Guayaquil', 'O-', 'Sulfas, Aspirina'),
('0967890006', 'Isabel', 'Sánchez Vera', '2010-09-10', 'F', '0991234572', 'isanchez@email.com', 'Alborada 10ma Etapa, Guayaquil', 'A-', 'Ninguna'),
('0978901007', 'Miguel', 'Ramírez Castro', '1975-12-05', 'M', '0991234573', 'mramirez@email.com', 'Centenario, Calle 6ta, Guayaquil', 'B-', 'Látex'),
('0989012008', 'Gabriela', 'Flores Mendoza', '2005-04-14', 'F', '0991234574', 'gflores@email.com', 'La Garzota, Mz. 78, Guayaquil', 'O+', 'Ninguna'),
('0990123009', 'Fernando', 'Vega Paredes', '1988-08-20', 'M', '0991234575', 'fvega@email.com', 'Mapasingue Este, Mz. 234, Guayaquil', 'A+', 'Ibuprofeno'),
('0901234010', 'Mónica', 'Herrera Guzmán', '1992-02-28', 'F', '0991234576', 'mherrera@email.com', 'Samborondón, Vía a la Costa, Guayaquil', 'AB-', 'Ninguna'),
('0923456011', 'Andrea', 'Salazar Ruiz', '1998-06-12', 'F', '0991234577', 'asalazar@email.com', 'Ceibos Norte, Mz. 102, Guayaquil', 'O+', 'Ninguna'),
('0934567012', 'Ricardo', 'Moreno Paz', '1980-09-03', 'M', '0991234578', 'rmoreno@email.com', 'Kennedy Vieja, Calle 5ta, Guayaquil', 'B+', 'Ninguna'),
('0945678013', 'Valeria', 'Suárez León', '2008-11-25', 'F', '0991234579', 'vsuarez@email.com', 'La Garzota 2, Mz. 210, Guayaquil', 'A+', 'Lactosa'),
('0956789014', 'Andrés', 'Paredes Vera', '1986-01-17', 'M', '0991234580', 'aparedes@email.com', 'Urdesa Norte, Av. Las Monjas, Guayaquil', 'AB+', 'Ninguna'),
('0967890015', 'Daniela', 'Castro Méndez', '1993-04-08', 'F', '0991234581', 'dcastro@email.com', 'Samborondón, Km 2.5, Guayaquil', 'O-', 'Ninguna');

-- Insert documentos_paciente
INSERT INTO documentos_paciente (paciente_id, nombre_archivo, tipo_documento, ruta_archivo) VALUES
(1, 'historia_clinica_jperez.pdf', 'HISTORIA_CLINICA', 'documents/paciente_1/historia_clinica_jperez.pdf'),
(1, 'examen_sangre_2024.pdf', 'EXAMEN', 'documents/paciente_1/examen_sangre_2024.pdf'),
(2, 'historia_clinica_lgarcia.pdf', 'HISTORIA_CLINICA', 'documents/paciente_2/historia_clinica_lgarcia.pdf'),
(3, 'ecocardiograma_plopez.pdf', 'EXAMEN', 'documents/paciente_3/ecocardiograma_plopez.pdf'),
(3, 'receta_cardio_oct2024.pdf', 'RECETA', 'documents/paciente_3/receta_cardio_oct2024.pdf'),
(4, 'historia_clinica_cdiaz.pdf', 'HISTORIA_CLINICA', 'documents/paciente_4/historia_clinica_cdiaz.pdf'),
(5, 'examen_prenatal_rmartinez.pdf', 'EXAMEN', 'documents/paciente_5/examen_prenatal_rmartinez.pdf'),
(7, 'radiografia_rodilla_mramirez.pdf', 'EXAMEN', 'documents/paciente_7/radiografia_rodilla_mramirez.pdf'),
(8, 'evaluacion_psiquiatrica_gflores.pdf', 'OTRO', 'documents/paciente_8/evaluacion_psiquiatrica_gflores.pdf'),
(9, 'plan_nutricional_fvega.pdf', 'OTRO', 'documents/paciente_9/plan_nutricional_fvega.pdf'),
(10, 'historia_clinica_mherrera.pdf', 'HISTORIA_CLINICA', 'documents/paciente_10/historia_clinica_mherrera.pdf'),
(11, 'historia_clinica_asalazar.pdf', 'HISTORIA_CLINICA', 'documents/paciente_11/historia_clinica_asalazar.pdf'),
(13, 'historia_clinica_vsuarez.pdf', 'HISTORIA_CLINICA', 'documents/paciente_13/historia_clinica_vsuarez.pdf'),
(15, 'examen_sangre_dcastro.pdf', 'EXAMEN', 'documents/paciente_15/examen_sangre_dcastro.pdf');
