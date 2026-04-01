CREATE TABLE inv_reservas (
    id                  BIGSERIAL PRIMARY KEY,
    empresa_id          BIGINT NOT NULL REFERENCES inv_empresas(id),
    contenedor_id       BIGINT NOT NULL REFERENCES inv_contenedores(id),
    codigo_barras       VARCHAR(100) NOT NULL,
    producto_id         BIGINT NOT NULL,
    bodega_id           BIGINT NOT NULL REFERENCES inv_bodegas(id),
    cantidad_reservada  NUMERIC(18,6) NOT NULL,
    cantidad_cumplida   NUMERIC(18,6) NOT NULL DEFAULT 0,
    estado              VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    tipo_referencia     VARCHAR(50) NOT NULL,
    referencia_id       BIGINT NOT NULL,
    referencia_linea_id BIGINT,
    fecha_expiracion    TIMESTAMPTZ,
    creado_por          BIGINT,
    creado_en           TIMESTAMPTZ DEFAULT now(),
    modificado_en       TIMESTAMPTZ DEFAULT now(),
    version             BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_reservas_contenedor ON inv_reservas(contenedor_id, estado);
CREATE INDEX idx_reservas_referencia ON inv_reservas(tipo_referencia, referencia_id);
CREATE INDEX idx_reservas_expiracion ON inv_reservas(fecha_expiracion) WHERE estado IN ('PENDIENTE', 'PARCIAL');
