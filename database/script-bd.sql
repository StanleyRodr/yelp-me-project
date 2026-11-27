-- =====================================================================
--  YelpMe — Script de base de datos
--  MySQL 8 · Base de datos: YelpMe
--
--  Política de datos: NO se borran registros importantes, se deshabilitan.
--    - usuarios      → activo = 0 (solo un admin reactiva)
--    - servicios     → activo = 0
--    - solicitudes   → nunca se borran; terminan en un estado final
--    - strikes       → se anulan (activo = 0 + datos de anulación)
--    - suspensiones  → historial, nunca se borran
--  Se borran físicamente: contactos, bloqueos (al desbloquear),
--  solicitud_servicio (al quitar un servicio) y tokens de sesión.
--
--  Las FK usan ON DELETE RESTRICT: la BD impide borrar lo que debe
--  deshabilitarse.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS YelpMe
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE YelpMe;

-- ---------------------------------------------------------------------
-- 1. usuarios: cuentas de usuarios (Android) y administradores (web)
-- ---------------------------------------------------------------------
CREATE TABLE usuarios (
    id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(100)  NOT NULL,
    apellido_paterno  VARCHAR(60)   NOT NULL,
    apellido_materno  VARCHAR(60)   NULL,
    telefono          VARCHAR(15)   NOT NULL,
    correo            VARCHAR(150)  NOT NULL,
    password          VARCHAR(255)  NOT NULL,
    rol               ENUM('admin', 'usuario') NOT NULL DEFAULT 'usuario',
    foto_perfil       VARCHAR(255)  NULL,
    fcm_token         VARCHAR(255)  NULL,
    activo            BOOLEAN       NOT NULL DEFAULT TRUE,
    deshabilitado_en  TIMESTAMP     NULL,
    created_at        TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuarios_telefono UNIQUE (telefono),
    CONSTRAINT uq_usuarios_correo   UNIQUE (correo),
    INDEX idx_usuarios_rol_activo (rol, activo)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2. contactos: agenda del usuario (CRUD de Android)
--    usuario_contacto_id se llena si el teléfono pertenece a un usuario
-- ---------------------------------------------------------------------
CREATE TABLE contactos (
    id                   BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id           BIGINT UNSIGNED NOT NULL,
    nombre               VARCHAR(100)    NOT NULL,
    telefono             VARCHAR(15)     NOT NULL,
    parentesco           VARCHAR(50)     NOT NULL,
    usuario_contacto_id  BIGINT UNSIGNED NULL,
    favorito             BOOLEAN         NOT NULL DEFAULT FALSE,  -- máximo uno por usuario (plan 010)
    created_at           TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_contactos_usuario_telefono UNIQUE (usuario_id, telefono),
    CONSTRAINT fk_contactos_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_contactos_usuario_contacto
        FOREIGN KEY (usuario_contacto_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    INDEX idx_contactos_usuario_contacto (usuario_contacto_id),
    INDEX idx_contactos_usuario_favorito (usuario_id, favorito)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. bloqueos: usuario_id bloquea a bloqueado_id (impide volver a vincular)
--    Al desbloquear se borra el registro.
-- ---------------------------------------------------------------------
CREATE TABLE bloqueos (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id    BIGINT UNSIGNED NOT NULL,
    bloqueado_id  BIGINT UNSIGNED NOT NULL,
    created_at    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_bloqueos_par UNIQUE (usuario_id, bloqueado_id),   -- usuario_id <> bloqueado_id se valida en backend
    CONSTRAINT fk_bloqueos_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_bloqueos_bloqueado
        FOREIGN KEY (bloqueado_id) REFERENCES usuarios (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 4. servicios: catálogo fijo de servicios de emergencia
--    icono = nombre del ícono en Material Symbols
-- ---------------------------------------------------------------------
CREATE TABLE servicios (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL,
    icono       VARCHAR(50)  NOT NULL,
    color       VARCHAR(7)   NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_servicios_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 5. solicitudes: peticiones de ayuda con ubicación y seguimiento
--    Las transiciones de estado las valida el backend (plan principal 7.2)
-- ---------------------------------------------------------------------
CREATE TABLE solicitudes (
    id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id        BIGINT UNSIGNED NOT NULL,
    titulo            VARCHAR(150)    NOT NULL,
    descripcion       TEXT            NULL,
    latitud           DECIMAL(10, 8)  NOT NULL,
    longitud          DECIMAL(11, 8)  NOT NULL,
    ubicacion_nombre  VARCHAR(255)    NULL,
    estado            ENUM('Pendiente', 'Enviado', 'Rechazado', 'Cancelado', 'Terminado')
                      NOT NULL DEFAULT 'Pendiente',
    admin_id          BIGINT UNSIGNED NULL,     -- admin que la atendió
    cerrado_por_id    BIGINT UNSIGNED NULL,     -- quién la llevó a un estado final
    motivo            TEXT            NULL,     -- obligatorio en Rechazado/Cancelado (backend)
    enviado_en        TIMESTAMP       NULL,
    cerrado_en        TIMESTAMP       NULL,
    created_at        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_solicitudes_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_solicitudes_admin
        FOREIGN KEY (admin_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_solicitudes_cerrado_por
        FOREIGN KEY (cerrado_por_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    INDEX idx_solicitudes_estado_fecha (estado, created_at),
    INDEX idx_solicitudes_usuario (usuario_id, created_at)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 6. solicitud_servicio: servicios requeridos por solicitud (M:N)
--    Al quitar un servicio se borra la fila.
-- ---------------------------------------------------------------------
CREATE TABLE solicitud_servicio (
    solicitud_id  BIGINT UNSIGNED NOT NULL,
    servicio_id   BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (solicitud_id, servicio_id),
    CONSTRAINT fk_solser_solicitud
        FOREIGN KEY (solicitud_id) REFERENCES solicitudes (id) ON DELETE RESTRICT,
    CONSTRAINT fk_solser_servicio
        FOREIGN KEY (servicio_id) REFERENCES servicios (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 7. strikes: sanciones por mal uso
--    activo = 1 cuenta para la suspensión (3 activos → suspensión).
--    Pasa a 0 al rehabilitar la cuenta o al anularse por error
--    (en ese caso se llenan los campos anulado_*).
-- ---------------------------------------------------------------------
CREATE TABLE strikes (
    id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id        BIGINT UNSIGNED NOT NULL,   -- sancionado
    admin_id          BIGINT UNSIGNED NOT NULL,   -- quién lo dio
    solicitud_id      BIGINT UNSIGNED NULL,       -- NULL = reporte manual
    motivo            TEXT            NOT NULL,
    activo            BOOLEAN         NOT NULL DEFAULT TRUE,
    anulado_por_id    BIGINT UNSIGNED NULL,
    motivo_anulacion  TEXT            NULL,
    anulado_en        TIMESTAMP       NULL,
    created_at        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_strikes_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_strikes_admin
        FOREIGN KEY (admin_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_strikes_solicitud
        FOREIGN KEY (solicitud_id) REFERENCES solicitudes (id) ON DELETE RESTRICT,
    CONSTRAINT fk_strikes_anulado_por
        FOREIGN KEY (anulado_por_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    INDEX idx_strikes_usuario_activo (usuario_id, activo)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 8. suspensiones: historial de suspensiones y rehabilitaciones
--    fecha_rehabilitacion NULL = suspensión vigente
--    suspendido_por_id NULL = suspensión automática por 3 strikes
-- ---------------------------------------------------------------------
CREATE TABLE suspensiones (
    id                    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id            BIGINT UNSIGNED NOT NULL,
    suspendido_por_id     BIGINT UNSIGNED NULL,
    motivo                TEXT            NOT NULL,
    fecha_suspension      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    rehabilitado_por_id   BIGINT UNSIGNED NULL,
    fecha_rehabilitacion  TIMESTAMP       NULL,
    CONSTRAINT fk_susp_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_susp_suspendido_por
        FOREIGN KEY (suspendido_por_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_susp_rehabilitado_por
        FOREIGN KEY (rehabilitado_por_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    INDEX idx_susp_usuario_vigente (usuario_id, fecha_rehabilitacion)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 9. personal_access_tokens: tokens de Laravel Sanctum (estructura estándar)
--    Polimórfica (tokenable_type/tokenable_id), sin FK.
-- ---------------------------------------------------------------------
CREATE TABLE personal_access_tokens (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tokenable_type  VARCHAR(255)    NOT NULL,
    tokenable_id    BIGINT UNSIGNED NOT NULL,
    name            TEXT            NOT NULL,
    token           VARCHAR(64)     NOT NULL,
    abilities       TEXT            NULL,
    last_used_at    TIMESTAMP       NULL,
    expires_at      TIMESTAMP       NULL,
    created_at      TIMESTAMP       NULL,
    updated_at      TIMESTAMP       NULL,
    CONSTRAINT uq_pat_token UNIQUE (token),
    INDEX idx_pat_tokenable (tokenable_type, tokenable_id),
    INDEX idx_pat_expires_at (expires_at)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 10. codigos_recuperacion: códigos de 6 dígitos para recuperar la contraseña
--     (plan principal 5.1). Se guarda el hash, nunca el código. No se borran:
--     vigente = usado_en IS NULL AND expira_en > ahora AND intentos < 5.
--     Pedir otro código marca los anteriores como usados.
-- ---------------------------------------------------------------------
CREATE TABLE codigos_recuperacion (
    id          BIGINT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    usuario_id  BIGINT UNSIGNED  NOT NULL,
    codigo      VARCHAR(255)     NOT NULL,
    intentos    TINYINT UNSIGNED NOT NULL DEFAULT 0,
    expira_en   TIMESTAMP        NOT NULL,
    usado_en    TIMESTAMP        NULL,
    created_at  TIMESTAMP        NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_codrec_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    INDEX idx_codrec_usuario (usuario_id, usado_en)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 11. domicilios: domicilio único del usuario con su punto en el mapa
--     (plan 009). 1:1 con usuarios; se borra físicamente al quitarlo.
-- ---------------------------------------------------------------------
CREATE TABLE domicilios (
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id     BIGINT UNSIGNED NOT NULL,
    calle          VARCHAR(120)    NOT NULL,
    numero         VARCHAR(20)     NOT NULL,
    colonia        VARCHAR(100)    NOT NULL,
    codigo_postal  CHAR(5)         NOT NULL,
    referencia     VARCHAR(255)    NULL,
    latitud        DECIMAL(10, 8)  NOT NULL,
    longitud       DECIMAL(11, 8)  NOT NULL,
    created_at     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_domicilios_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_domicilios_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =====================================================================
--  Datos iniciales
-- =====================================================================

-- Administrador inicial
--   correo:     admin@yelpme.com
--   contraseña: Admin123*   (CAMBIARLA tras el primer inicio de sesión)
INSERT INTO usuarios (nombre, apellido_paterno, apellido_materno, telefono, correo, password, rol)
VALUES ('Administrador', 'General', NULL, '0000000000', 'admin@yelpme.com',
        '$2y$10$IYbySxhJpZ3.OXhDwz0b/egNNxS1lsCzgcrPNwqvmHakwLON.D31e', 'admin');

-- Catálogo de servicios (íconos de Material Symbols)
INSERT INTO servicios (nombre, icono, color) VALUES
    ('Policía',              'local_police',       '#243C5A'),
    ('Ambulancia',           'ambulance',          '#FF0000'),
    ('Bomberos',             'fire_truck',         '#DC2626'),
    ('Tránsito',             'traffic',            '#F59E0B'),
    ('Protección Civil',     'health_and_safety',  '#EA580C'),
    ('Asistencia Ciudadana', 'support',            '#10B981');
