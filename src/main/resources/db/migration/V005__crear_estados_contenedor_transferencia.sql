CREATE TABLE inv_estados_contenedor (
    id                    BIGSERIAL PRIMARY KEY,
    codigo                VARCHAR(30) NOT NULL UNIQUE,
    nombre                VARCHAR(100) NOT NULL,
    permite_picking       BOOLEAN NOT NULL DEFAULT false,
    permite_transferencia BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE inv_estados_transferencia (
    id          BIGSERIAL PRIMARY KEY,
    codigo      VARCHAR(30) NOT NULL UNIQUE,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500)
);
