CREATE TABLE inv_lotes (
    id               BIGSERIAL PRIMARY KEY,
    empresa_id       BIGINT NOT NULL REFERENCES inv_empresas(id),
    numero_lote      VARCHAR(100) NOT NULL,
    producto_id      BIGINT NOT NULL,
    fecha_produccion DATE,
    fecha_vencimiento DATE,
    proveedor_id     BIGINT,
    estado           VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    notas            TEXT,
    creado_en        TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por       BIGINT,
    modificado_en    TIMESTAMPTZ NOT NULL DEFAULT now(),
    modificado_por   BIGINT,
    activo           BOOLEAN NOT NULL DEFAULT true,
    version          BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_lotes_empresa_producto ON inv_lotes(empresa_id, producto_id);
CREATE INDEX idx_lotes_numero ON inv_lotes(empresa_id, numero_lote);
