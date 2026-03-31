CREATE TABLE inv_configuracion_producto (
    id                  BIGSERIAL PRIMARY KEY,
    empresa_id          BIGINT NOT NULL REFERENCES inv_empresas(id),
    producto_id         BIGINT NOT NULL,
    maneja_lote         BOOLEAN NOT NULL DEFAULT false,
    maneja_vencimiento  BOOLEAN NOT NULL DEFAULT false,
    tolerancia_merma    NUMERIC(18,6) NOT NULL DEFAULT 0,
    unidad_base_id      BIGINT REFERENCES inv_unidades(id),
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por          BIGINT,
    modificado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    modificado_por      BIGINT,
    activo              BOOLEAN NOT NULL DEFAULT true,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_config_producto_empresa UNIQUE (empresa_id, producto_id)
);
