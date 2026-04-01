CREATE TABLE inv_unidades (
    id          BIGSERIAL PRIMARY KEY,
    empresa_id  BIGINT NOT NULL REFERENCES inv_empresas(id),
    codigo      VARCHAR(20) NOT NULL,
    nombre      VARCHAR(100) NOT NULL,
    abreviatura VARCHAR(10),
    activo      BOOLEAN NOT NULL DEFAULT true,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    modificado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_unidades_empresa_codigo UNIQUE (empresa_id, codigo)
);

CREATE INDEX idx_unidades_empresa ON inv_unidades(empresa_id);

CREATE TABLE inv_conversiones (
    id                BIGSERIAL PRIMARY KEY,
    empresa_id        BIGINT NOT NULL REFERENCES inv_empresas(id),
    unidad_origen_id  BIGINT NOT NULL REFERENCES inv_unidades(id),
    unidad_destino_id BIGINT NOT NULL REFERENCES inv_unidades(id),
    factor_conversion NUMERIC(18,6) NOT NULL,
    tipo_operacion    VARCHAR(15) NOT NULL,
    producto_id       BIGINT,
    activo            BOOLEAN NOT NULL DEFAULT true,
    creado_en         TIMESTAMPTZ NOT NULL DEFAULT now(),
    modificado_en     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_conversiones UNIQUE (empresa_id, unidad_origen_id, unidad_destino_id, producto_id)
);

CREATE INDEX idx_conversiones_empresa ON inv_conversiones(empresa_id);
