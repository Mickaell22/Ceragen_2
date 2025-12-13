-- Initial seed data for Ceragen System (Fixed version)
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

-- Insert especialidades (using new schema structure)
INSERT INTO especialidades (nombre, codigo, descripcion, duracion_estandar_min, tarifa_base, estado, usuario_creador_id) VALUES
('Medicina General', 'MED-GEN', 'Atencion primaria de salud', 30, 50.00, 'ACTIVO', 1),
('Pediatria', 'PED', 'Cuidado de bebes, ninos y adolescentes', 30, 60.00, 'ACTIVO', 1),
('Cardiologia', 'CARD', 'Enfermedades del corazon', 45, 80.00, 'ACTIVO', 1),
('Dermatologia', 'DERM', 'Enfermedades de la piel', 30, 65.00, 'ACTIVO', 1),
('Ginecologia', 'GIN', 'Salud reproductiva femenina', 30, 70.00, 'ACTIVO', 1),
('Traumatologia', 'TRAUM', 'Lesiones musculo-esqueleticas', 30, 75.00, 'ACTIVO', 1),
('Oftalmologia', 'OFT', 'Enfermedades de los ojos', 30, 55.00, 'ACTIVO', 1),
('Psiquiatria', 'PSI', 'Trastornos mentales', 60, 90.00, 'ACTIVO', 1),
('Nutricion', 'NUT', 'Asesoramiento nutricional', 30, 45.00, 'ACTIVO', 1),
('Odontologia', 'ODO', 'Enfermedades dentales', 30, 40.00, 'ACTIVO', 1);

-- Insert profesionales (using new schema structure)
INSERT INTO profesionales (tipo_usuario_registra, cedula, nombres, apellidos, email, codigo_pais, celular, numero_licencia, modalidad_atencion, activo, usuario_id) VALUES
('ADMIN', '0912345678', 'Carlos', 'Mendoza Garcia', 'cmendoza@ceragen.com', '+593', '987654321', 'MED-2020-001', 'PRESENCIAL', TRUE, 4),
('ADMIN', '0923456789', 'Maria', 'Rodriguez Lopez', 'mrodriguez@ceragen.com', '+593', '987654322', 'MED-2019-045', 'PRESENCIAL', TRUE, 5),
('ADMIN', '0934567890', 'Jorge', 'Sanchez Perez', 'jsanchez@ceragen.com', '+593', '987654323', 'MED-2018-089', 'MIXTA', TRUE, 6),
('ADMIN', '0945678901', 'Ana', 'Gomez Torres', 'agomez@ceragen.com', '+593', '987654324', 'MED-2021-023', 'PRESENCIAL', TRUE, 7),
('ADMIN', '0956789012', 'Luis', 'Martinez Silva', 'lmartinez@ceragen.com', '+593', '987654325', 'MED-2017-112', 'PRESENCIAL', TRUE, 8),
('ADMIN', '0967890123', 'Patricia', 'Flores Ruiz', 'pflores@ceragen.com', '+593', '987654326', 'MED-2020-067', 'TELECONSULTA', TRUE, NULL),
('ADMIN', '0978901234', 'Roberto', 'Castro Vargas', 'rcastro@ceragen.com', '+593', '987654327', 'MED-2019-134', 'PRESENCIAL', TRUE, NULL),
('ADMIN', '0989012345', 'Carmen', 'Vega Morales', 'cvega@ceragen.com', '+593', '987654328', 'MED-2018-178', 'MIXTA', TRUE, NULL),
('ADMIN', '0990123456', 'Diego', 'Herrera Guzman', 'dherrera@ceragen.com', '+593', '987654329', 'NUT-2021-045', 'PRESENCIAL', TRUE, NULL),
('ADMIN', '0901234567', 'Sofia', 'Ramirez Ortiz', 'sramirez@ceragen.com', '+593', '987654330', 'ODO-2020-089', 'PRESENCIAL', TRUE, NULL);

-- Assign specialties to professionals (profesional_especialidades)
INSERT INTO profesional_especialidades (profesional_id, especialidad_id, es_principal) VALUES
(1, 1, TRUE),   -- Carlos Mendoza -> Medicina General
(2, 2, TRUE),   -- Maria Rodriguez -> Pediatria
(3, 3, TRUE),   -- Jorge Sanchez -> Cardiologia
(4, 4, TRUE),   -- Ana Gomez -> Dermatologia
(5, 5, TRUE),   -- Luis Martinez -> Ginecologia
(6, 6, TRUE),   -- Patricia Flores -> Traumatologia
(7, 7, TRUE),   -- Roberto Castro -> Oftalmologia
(8, 8, TRUE),   -- Carmen Vega -> Psiquiatria
(9, 9, TRUE),   -- Diego Herrera -> Nutricion
(10, 10, TRUE); -- Sofia Ramirez -> Odontologia

-- Insert profesional availability (sample schedules)
INSERT INTO profesional_disponibilidad (profesional_id, dia_semana, hora_inicio, hora_fin) VALUES
-- Carlos Mendoza (Medicina General)
(1, 'LUNES', '08:00:00', '12:00:00'),
(1, 'LUNES', '14:00:00', '18:00:00'),
(1, 'MIERCOLES', '08:00:00', '12:00:00'),
(1, 'MIERCOLES', '14:00:00', '18:00:00'),
(1, 'VIERNES', '08:00:00', '12:00:00'),

-- Maria Rodriguez (Pediatria)
(2, 'MARTES', '08:00:00', '13:00:00'),
(2, 'JUEVES', '08:00:00', '13:00:00'),
(2, 'VIERNES', '14:00:00', '18:00:00'),

-- Jorge Sanchez (Cardiologia)
(3, 'LUNES', '09:00:00', '13:00:00'),
(3, 'MIERCOLES', '09:00:00', '13:00:00'),
(3, 'VIERNES', '09:00:00', '13:00:00');

-- Insert pacientes
INSERT INTO pacientes (cedula, nombres, apellidos, fecha_nacimiento, genero, telefono, email, direccion, grupo_sanguineo, alergias) VALUES
('0912345001', 'Juan', 'Perez Gomez', '1985-03-15', 'M', '0991234567', 'jperez@email.com', 'Av. 9 de Octubre y Malecon, Guayaquil', 'O+', 'Penicilina'),
('0923456002', 'Laura', 'Garcia Morales', '1990-07-22', 'F', '0991234568', 'lgarcia@email.com', 'Cdla. Kennedy Norte, Mz. 12, Guayaquil', 'A+', 'Ninguna'),
('0934567003', 'Pedro', 'Lopez Silva', '1978-11-30', 'M', '0991234569', 'plopez@email.com', 'Urdesa Central, Calle 3ra, Guayaquil', 'B+', 'Polen, Mariscos'),
('0945678004', 'Carmen', 'Diaz Torres', '1995-01-18', 'F', '0991234570', 'cdiaz@email.com', 'Sauces 4, Mz. 345, Guayaquil', 'AB+', 'Ninguna'),
('0956789005', 'Roberto', 'Martinez Ruiz', '1982-05-25', 'M', '0991234571', 'rmartinez@email.com', 'Ceibos, Etapa 2, Mz. 56, Guayaquil', 'O-', 'Sulfas, Aspirina'),
('0967890006', 'Isabel', 'Sanchez Vera', '2010-09-10', 'F', '0991234572', 'isanchez@email.com', 'Alborada 10ma Etapa, Guayaquil', 'A-', 'Ninguna'),
('0978901007', 'Miguel', 'Ramirez Castro', '1975-12-05', 'M', '0991234573', 'mramirez@email.com', 'Centenario, Calle 6ta, Guayaquil', 'B-', 'Latex'),
('0989012008', 'Gabriela', 'Flores Mendoza', '2005-04-14', 'F', '0991234574', 'gflores@email.com', 'La Garzota, Mz. 78, Guayaquil', 'O+', 'Ninguna'),
('0990123009', 'Fernando', 'Vega Paredes', '1988-08-20', 'M', '0991234575', 'fvega@email.com', 'Mapasingue Este, Mz. 234, Guayaquil', 'A+', 'Ibuprofeno'),
('0901234010', 'Monica', 'Herrera Guzman', '1992-02-28', 'F', '0991234576', 'mherrera@email.com', 'Samborondon, Via a la Costa, Guayaquil', 'AB-', 'Ninguna'),
('0923456011', 'Andrea', 'Salazar Ruiz', '1998-06-12', 'F', '0991234577', 'asalazar@email.com', 'Ceibos Norte, Mz. 102, Guayaquil', 'O+', 'Ninguna'),
('0934567012', 'Ricardo', 'Moreno Paz', '1980-09-03', 'M', '0991234578', 'rmoreno@email.com', 'Kennedy Vieja, Calle 5ta, Guayaquil', 'B+', 'Ninguna'),
('0945678013', 'Valeria', 'Suarez Leon', '2008-11-25', 'F', '0991234579', 'vsuarez@email.com', 'La Garzota 2, Mz. 210, Guayaquil', 'A+', 'Lactosa'),
('0956789014', 'Andres', 'Paredes Vera', '1986-01-17', 'M', '0991234580', 'aparedes@email.com', 'Urdesa Norte, Av. Las Monjas, Guayaquil', 'AB+', 'Ninguna'),
('0967890015', 'Daniela', 'Castro Mendez', '1993-04-08', 'F', '0991234581', 'dcastro@email.com', 'Samborondon, Km 2.5, Guayaquil', 'O-', 'Ninguna');

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

-- Insert clientes (for invoicing)
INSERT INTO clientes (cedula, nombres, apellidos, telefono, email, direccion, activo) VALUES
('0912345001', 'Juan', 'Perez Gomez', '0991234567', 'jperez@email.com', 'Av. 9 de Octubre y Malecon, Guayaquil', TRUE),
('0923456002', 'Laura', 'Garcia Morales', '0991234568', 'lgarcia@email.com', 'Cdla. Kennedy Norte, Mz. 12, Guayaquil', TRUE),
('0934567003', 'Pedro', 'Lopez Silva', '0991234569', 'plopez@email.com', 'Urdesa Central, Calle 3ra, Guayaquil', TRUE),
('0945678004', 'Carmen', 'Diaz Torres', '0991234570', 'cdiaz@email.com', 'Sauces 4, Mz. 345, Guayaquil', TRUE),
('0956789005', 'Roberto', 'Martinez Ruiz', '0991234571', 'rmartinez@email.com', 'Ceibos, Etapa 2, Mz. 56, Guayaquil', TRUE);

-- Insert sample facturas
INSERT INTO facturas (numero_factura, cliente_id, fecha_emision, ciudad, subtotal, iva, descuento, total, metodo_pago, estado) VALUES
('FAC-2025-001', 1, '2025-11-05 09:00:00', 'Guayaquil', 50.00, 6.00, 0.00, 56.00, 'EFECTIVO', 'ACTIVA'),
('FAC-2025-002', 2, '2025-11-05 10:30:00', 'Guayaquil', 60.00, 7.20, 0.00, 67.20, 'TARJETA', 'ACTIVA'),
('FAC-2025-003', 3, '2025-11-06 14:00:00', 'Guayaquil', 80.00, 9.60, 0.00, 89.60, 'TRANSFERENCIA', 'ACTIVA'),
('FAC-2025-004', 4, '2025-11-06 15:30:00', 'Guayaquil', 65.00, 7.80, 0.00, 72.80, 'EFECTIVO', 'ACTIVA'),
('FAC-2025-005', 5, '2025-11-07 08:30:00', 'Guayaquil', 70.00, 8.40, 5.00, 73.40, 'TARJETA', 'ACTIVA');

-- Insert sample citas
INSERT INTO citas (paciente_id, profesional_id, fecha_hora, motivo, estado, costo, factura_id, observaciones) VALUES
(1, 1, '2025-11-05 09:00:00', 'Chequeo general de rutina', 'ATENDIDA', 50.00, 1, 'Paciente en buen estado general'),
(2, 2, '2025-11-05 10:30:00', 'Control pediatrico', 'ATENDIDA', 60.00, 2, 'Desarrollo normal para su edad'),
(3, 3, '2025-11-06 14:00:00', 'Dolor en el pecho', 'ATENDIDA', 80.00, 3, 'Se solicitan examenes cardiologicos'),
(4, 4, '2025-11-06 15:30:00', 'Consulta por acne', 'ATENDIDA', 65.00, 4, 'Se receto tratamiento topico'),
(5, 5, '2025-11-07 08:30:00', 'Control prenatal', 'CONFIRMADA', 70.00, 5, 'Embarazo de 20 semanas, todo normal');

-- Insert detalle_factura
INSERT INTO detalle_factura (factura_id, cita_id, precio_unitario, cantidad) VALUES
(1, 1, 50.00, 1),
(2, 2, 60.00, 1),
(3, 3, 80.00, 1),
(4, 4, 65.00, 1),
(5, 5, 70.00, 1);
