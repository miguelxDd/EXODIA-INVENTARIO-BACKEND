CREATE TABLE inv_maximos_minimos (
    id                      BIGSERIAL PRIMARY KEY,
    empresa_id              BIGINT NOT NULL REFERENCES inv_empresas(id),
    producto_id             BIGINT NOT NULL,
    bodega_id               BIGINT NOT NULL REFERENCES inv_bodegas(id),
    unidad_id               BIGINT NOT NULL REFERENCES inv_unidades(id),
    stock_minimo            NUMERIC(18,6) NOT NULL,
    stock_maximo            NUMERIC(18,6) NOT NULL,
    punto_reorden           NUMERIC(18,6),
    stock_actual_calculado  NUMERIC(18,6),
    ultima_verificacion     TIMESTAMPTZ,
    creado_en               TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por              BIGINT,
    modificado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    modificado_por          BIGINT,
    activo                  BOOLEAN NOT NULL DEFAULT true,
    version                 BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_maxmin_empresa_producto_bodega UNIQUE (empresa_id, producto_id, bodega_id)
);
