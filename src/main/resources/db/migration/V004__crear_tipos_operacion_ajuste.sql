CREATE TABLE inv_tipos_operacion (
    id          BIGSERIAL PRIMARY KEY,
    codigo      VARCHAR(30) NOT NULL UNIQUE,
    nombre      VARCHAR(100) NOT NULL,
    signo       SMALLINT NOT NULL,
    descripcion VARCHAR(500),
    activo      BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE inv_tipos_ajuste (
    id          BIGSERIAL PRIMARY KEY,
    codigo      VARCHAR(30) NOT NULL UNIQUE,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    activo      BOOLEAN NOT NULL DEFAULT true
);
