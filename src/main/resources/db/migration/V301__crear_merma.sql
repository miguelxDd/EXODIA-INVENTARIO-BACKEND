CREATE TABLE inv_config_merma (
    id                  BIGSERIAL PRIMARY KEY,
    empresa_id          BIGINT NOT NULL REFERENCES inv_empresas(id),
    producto_id         BIGINT,
    bodega_id           BIGINT REFERENCES inv_bodegas(id),
    tipo_merma          VARCHAR(20) NOT NULL,
    porcentaje_merma    NUMERIC(8,4),
    cantidad_fija_merma NUMERIC(18,6),
    frecuencia_dias     INTEGER,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por          BIGINT,
    modificado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    modificado_por      BIGINT,
    activo              BOOLEAN NOT NULL DEFAULT true,
    version             BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE inv_registros_merma (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES inv_empresas(id),
    contenedor_id   BIGINT NOT NULL REFERENCES inv_contenedores(id),
    cantidad_merma  NUMERIC(18,6) NOT NULL,
    tipo_merma      VARCHAR(20) NOT NULL,
    config_merma_id BIGINT REFERENCES inv_config_merma(id),
    operacion_id    BIGINT REFERENCES inv_operaciones(id),
    comentarios     TEXT,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por      BIGINT
);

CREATE INDEX idx_registros_merma_contenedor ON inv_registros_merma(contenedor_id);
