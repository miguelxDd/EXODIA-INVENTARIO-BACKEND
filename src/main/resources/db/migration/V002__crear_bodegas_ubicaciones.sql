CREATE TABLE inv_bodegas (
    id                    BIGSERIAL PRIMARY KEY,
    empresa_id            BIGINT NOT NULL REFERENCES inv_empresas(id),
    codigo                VARCHAR(30) NOT NULL,
    nombre                VARCHAR(200) NOT NULL,
    direccion             VARCHAR(500),
    ciudad                VARCHAR(100),
    pais                  VARCHAR(100),
    ubicacion_standby_id  BIGINT,
    es_producto_terminado BOOLEAN NOT NULL DEFAULT false,
    es_consignacion       BOOLEAN NOT NULL DEFAULT false,
    creado_en             TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por            BIGINT,
    modificado_en         TIMESTAMPTZ NOT NULL DEFAULT now(),
    modificado_por        BIGINT,
    activo                BOOLEAN NOT NULL DEFAULT true,
    version               BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_bodegas_empresa_codigo UNIQUE (empresa_id, codigo)
);

CREATE INDEX idx_bodegas_empresa ON inv_bodegas(empresa_id);

CREATE TABLE inv_ubicaciones (
    id              BIGSERIAL PRIMARY KEY,
    bodega_id       BIGINT NOT NULL REFERENCES inv_bodegas(id),
    codigo          VARCHAR(50) NOT NULL,
    nombre          VARCHAR(200) NOT NULL,
    codigo_barras   VARCHAR(100),
    tipo_ubicacion  VARCHAR(30) NOT NULL DEFAULT 'GENERAL',
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por      BIGINT,
    modificado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    modificado_por  BIGINT,
    activo          BOOLEAN NOT NULL DEFAULT true,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_ubicaciones_bodega_codigo UNIQUE (bodega_id, codigo)
);

CREATE INDEX idx_ubicaciones_bodega ON inv_ubicaciones(bodega_id);

-- FK circular: standby location
ALTER TABLE inv_bodegas
    ADD CONSTRAINT fk_bodegas_ubicacion_standby
    FOREIGN KEY (ubicacion_standby_id) REFERENCES inv_ubicaciones(id);
