-- Database Schema for Ceragen System
-- Run this script to create all necessary tables

-- Table: usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL COMMENT 'bcrypt hashed password',
    rol ENUM('ADMIN', 'RECEPCIONISTA', 'MEDICO') NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: especialidades
CREATE TABLE IF NOT EXISTS especialidades (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    costo_consulta DECIMAL(10,2) NOT NULL,
    INDEX idx_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: pacientes
CREATE TABLE IF NOT EXISTS pacientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cedula VARCHAR(20) UNIQUE NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE,
    genero ENUM('M', 'F', 'OTRO'),
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion TEXT,
    grupo_sanguineo VARCHAR(5),
    alergias TEXT,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cedula (cedula),
    INDEX idx_nombre_completo (nombres, apellidos)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: documentos_paciente
CREATE TABLE IF NOT EXISTS documentos_paciente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    paciente_id INT NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    tipo_documento ENUM('HISTORIA_CLINICA', 'EXAMEN', 'RECETA', 'OTRO') NOT NULL,
    ruta_archivo VARCHAR(500) NOT NULL,
    fecha_subida DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE,
    INDEX idx_paciente (paciente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: profesionales
CREATE TABLE IF NOT EXISTS profesionales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cedula VARCHAR(20) UNIQUE NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    especialidad_id INT NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    numero_licencia VARCHAR(50),
    activo BOOLEAN DEFAULT TRUE,
    usuario_id INT NULL,
    FOREIGN KEY (especialidad_id) REFERENCES especialidades(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    INDEX idx_cedula (cedula),
    INDEX idx_especialidad (especialidad_id),
    INDEX idx_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: citas
CREATE TABLE IF NOT EXISTS citas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    paciente_id INT NOT NULL,
    profesional_id INT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    motivo TEXT,
    estado ENUM('PENDIENTE', 'CONFIRMADA', 'ATENDIDA', 'CANCELADA') DEFAULT 'PENDIENTE',
    observaciones TEXT,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE,
    FOREIGN KEY (profesional_id) REFERENCES profesionales(id),
    INDEX idx_paciente (paciente_id),
    INDEX idx_profesional (profesional_id),
    INDEX idx_fecha_hora (fecha_hora),
    INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: facturas
CREATE TABLE IF NOT EXISTS facturas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero_factura VARCHAR(50) UNIQUE NOT NULL,
    cita_id INT NOT NULL,
    paciente_id INT NOT NULL,
    monto_total DECIMAL(10,2) NOT NULL,
    estado ENUM('PENDIENTE', 'PAGADA', 'ANULADA') DEFAULT 'PENDIENTE',
    metodo_pago ENUM('EFECTIVO', 'TARJETA', 'TRANSFERENCIA'),
    fecha_emision DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_pago DATETIME NULL,
    FOREIGN KEY (cita_id) REFERENCES citas(id),
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE,
    INDEX idx_numero_factura (numero_factura),
    INDEX idx_estado (estado),
    INDEX idx_fecha_emision (fecha_emision)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
