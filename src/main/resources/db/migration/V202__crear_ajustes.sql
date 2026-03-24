CREATE TABLE inv_ajustes (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES inv_empresas(id),
    numero_ajuste   VARCHAR(50) NOT NULL,
    bodega_id       BIGINT NOT NULL REFERENCES inv_bodegas(id),
    tipo_ajuste_id  BIGINT NOT NULL REFERENCES inv_tipos_ajuste(id),
    motivo          TEXT,
    estado          VARCHAR(20) NOT NULL DEFAULT 'APLICADO',
    tipo_referencia VARCHAR(50),
    referencia_id   BIGINT,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por      BIGINT,
    modificado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    modificado_por  BIGINT,
    activo          BOOLEAN NOT NULL DEFAULT true,
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_ajustes_empresa ON inv_ajustes(empresa_id);

CREATE TABLE inv_ajuste_lineas (
    id                BIGSERIAL PRIMARY KEY,
    ajuste_id         BIGINT NOT NULL REFERENCES inv_ajustes(id),
    contenedor_id     BIGINT NOT NULL REFERENCES inv_contenedores(id),
    cantidad_anterior NUMERIC(18,6),
    cantidad_nueva    NUMERIC(18,6),
    cantidad_ajuste   NUMERIC(18,6) NOT NULL,
    precio_anterior   NUMERIC(18,6),
    precio_nuevo      NUMERIC(18,6),
    operacion_id      BIGINT REFERENCES inv_operaciones(id),
    creado_en         TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_ajuste_lineas_ajuste ON inv_ajuste_lineas(ajuste_id);
