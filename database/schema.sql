-- =====================================================================
--  Database Schema for Ceragen System
--  Este script DROP y recrea toda la BD
-- =====================================================================

DROP DATABASE IF EXISTS railway;
CREATE DATABASE railway CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE railway;

-- =====================================================================
--  TABLA: usuarios
-- =====================================================================
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL COMMENT 'bcrypt hashed password',
    rol ENUM('ADMIN', 'RECEPCIONISTA', 'MEDICO') NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  TABLA: especialidades  (MODULO ESPECIALIDAD SEGÚN REQUERIMIENTO)
-- =====================================================================
CREATE TABLE especialidades (
    id INT AUTO_INCREMENT PRIMARY KEY,

    -- Nombre de la especialidad: obligatorio, único, hasta 100
    nombre VARCHAR(100) NOT NULL,

    -- Código alfanumérico hasta 20, único
    codigo VARCHAR(20) NOT NULL,

    -- Descripción opcional, hasta 250 caracteres
    descripcion VARCHAR(250),

    -- Duración estándar de cita (minutos) entre 15 y 60, por defecto 30
    duracion_estandar_min INT NOT NULL DEFAULT 30,

    -- Tarifa base opcional: 0–9999.99 (2 decimales)
    tarifa_base DECIMAL(6,2) NULL,

    -- Estado: Activo / Inactivo, por defecto Activo
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',

    -- Auditoría: usuario creador y timestamp inicial
    usuario_creador_id INT NOT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (usuario_creador_id) REFERENCES usuarios(id),

    -- Reglas de unicidad
    UNIQUE KEY uk_especialidad_nombre (nombre),
    UNIQUE KEY uk_especialidad_codigo (codigo),

    INDEX idx_nombre (nombre),
    INDEX idx_codigo (codigo),
    INDEX idx_estado (estado),

    -- Restricciones de rango
    CHECK (duracion_estandar_min BETWEEN 15 AND 60),
    CHECK (tarifa_base IS NULL OR (tarifa_base >= 0 AND tarifa_base <= 9999.99))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  TABLA: pacientes
-- =====================================================================
CREATE TABLE pacientes (
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

-- =====================================================================
--  TABLA: documentos_paciente
-- =====================================================================
CREATE TABLE documentos_paciente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    paciente_id INT NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    tipo_documento ENUM('HISTORIA_CLINICA', 'EXAMEN', 'RECETA', 'OTRO') NOT NULL,
    ruta_archivo VARCHAR(500) NOT NULL,
    fecha_subida DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE,
    INDEX idx_paciente (paciente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  TABLA: clientes
-- =====================================================================
CREATE TABLE clientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cedula VARCHAR(20) UNIQUE NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cedula (cedula),
    INDEX idx_nombre_completo (nombres, apellidos)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  MÓDULO PROFESIONALES (SEGÚN REQUERIMIENTO ANTERIOR)
-- =====================================================================

-- Tabla principales datos del profesional
CREATE TABLE profesionales (
    id INT AUTO_INCREMENT PRIMARY KEY,

    -- Tipo de usuario que registra: Administrador o Recepcionista
    tipo_usuario_registra ENUM('ADMIN', 'RECEPCIONISTA') NOT NULL
        COMMENT 'Tipo de usuario que hizo el registro',

    -- Datos personales
    cedula VARCHAR(10) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,

    -- Correo electrónico único
    email VARCHAR(100) NOT NULL,

    -- Código país +593 y celular de 9 dígitos
    codigo_pais CHAR(4) NOT NULL DEFAULT '+593',
    celular VARCHAR(9) NOT NULL,

    -- Licencia / Registro médico (obligatoria y única)
    numero_licencia VARCHAR(30) NOT NULL,

    -- Modalidad de atención
    modalidad_atencion ENUM('PRESENCIAL', 'TELECONSULTA', 'MIXTA')
        NOT NULL DEFAULT 'PRESENCIAL',

    -- Estado del profesional
    activo BOOLEAN DEFAULT TRUE,

    -- Usuario de sistema asociado (login)
    usuario_id INT NULL,

    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,

    -- Unicidad
    UNIQUE KEY uk_profesionales_cedula (cedula),
    UNIQUE KEY uk_profesionales_email (email),
    UNIQUE KEY uk_profesionales_licencia (numero_licencia),

    INDEX idx_cedula (cedula),
    INDEX idx_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Relación muchos-a-muchos Profesional <-> Especialidad
CREATE TABLE profesional_especialidades (
    id INT AUTO_INCREMENT PRIMARY KEY,
    profesional_id INT NOT NULL,
    especialidad_id INT NOT NULL,
    es_principal BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (profesional_id) REFERENCES profesionales(id) ON DELETE CASCADE,
    FOREIGN KEY (especialidad_id) REFERENCES especialidades(id) ON DELETE RESTRICT,

    UNIQUE KEY uk_prof_esp (profesional_id, especialidad_id),
    INDEX idx_profesional (profesional_id),
    INDEX idx_especialidad (especialidad_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Disponibilidad de agenda del profesional
CREATE TABLE profesional_disponibilidad (
    id INT AUTO_INCREMENT PRIMARY KEY,
    profesional_id INT NOT NULL,
    dia_semana ENUM('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO','DOMINGO') NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,

    FOREIGN KEY (profesional_id) REFERENCES profesionales(id) ON DELETE CASCADE,

    UNIQUE KEY uk_prof_dia_franga (profesional_id, dia_semana, hora_inicio, hora_fin),
    INDEX idx_prof_dia (profesional_id, dia_semana)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  FACTURACIÓN Y CITAS
-- =====================================================================

-- TABLA: facturas
CREATE TABLE IF NOT EXISTS facturas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero_factura VARCHAR(50) UNIQUE NOT NULL,
    cliente_id INT NOT NULL,
    fecha_emision DATETIME DEFAULT CURRENT_TIMESTAMP,
    ciudad VARCHAR(100),
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    iva DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    descuento DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    metodo_pago ENUM('EFECTIVO', 'TARJETA', 'TRANSFERENCIA', 'OTRO') DEFAULT 'EFECTIVO',
    estado ENUM('ACTIVA', 'ANULADA') DEFAULT 'ACTIVA',
    FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE,
    INDEX idx_numero_factura (numero_factura),
    INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLA: citas
CREATE TABLE citas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    paciente_id INT NOT NULL,
    profesional_id INT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    motivo TEXT,
    estado ENUM('PENDIENTE', 'CONFIRMADA', 'ATENDIDA', 'CANCELADA') DEFAULT 'CONFIRMADA',
    costo DECIMAL(10,2),
    factura_id INT NOT NULL,
    observaciones TEXT,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE,
    FOREIGN KEY (profesional_id) REFERENCES profesionales(id),
    FOREIGN KEY (factura_id) REFERENCES facturas(id) ON DELETE CASCADE,
    INDEX idx_paciente (paciente_id),
    INDEX idx_profesional (profesional_id),
    INDEX idx_fecha_hora (fecha_hora),
    INDEX idx_estado (estado),
    INDEX idx_factura (factura_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLA: detalle_factura
CREATE TABLE detalle_factura (
    id INT AUTO_INCREMENT PRIMARY KEY,
    factura_id INT NOT NULL,
    cita_id INT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    total DECIMAL(10,2) GENERATED ALWAYS AS (precio_unitario * cantidad) STORED,
    FOREIGN KEY (factura_id) REFERENCES facturas(id) ON DELETE CASCADE,
    FOREIGN KEY (cita_id) REFERENCES citas(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
