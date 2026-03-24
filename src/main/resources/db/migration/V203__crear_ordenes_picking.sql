CREATE TABLE inv_ordenes_picking (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES inv_empresas(id),
    numero_orden    VARCHAR(50) NOT NULL,
    bodega_id       BIGINT NOT NULL REFERENCES inv_bodegas(id),
    tipo_picking    VARCHAR(30) NOT NULL,
    tipo_referencia VARCHAR(50),
    referencia_id   BIGINT,
    estado          VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    comentarios     TEXT,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por      BIGINT,
    modificado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    modificado_por  BIGINT,
    activo          BOOLEAN NOT NULL DEFAULT true,
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_ordenes_picking_empresa ON inv_ordenes_picking(empresa_id);

CREATE TABLE inv_picking_lineas (
    id                  BIGSERIAL PRIMARY KEY,
    orden_picking_id    BIGINT NOT NULL REFERENCES inv_ordenes_picking(id),
    producto_id         BIGINT NOT NULL,
    unidad_id           BIGINT NOT NULL REFERENCES inv_unidades(id),
    cantidad_solicitada NUMERIC(18,6) NOT NULL,
    cantidad_pickeada   NUMERIC(18,6) DEFAULT 0,
    contenedor_id       BIGINT REFERENCES inv_contenedores(id),
    operacion_id        BIGINT REFERENCES inv_operaciones(id),
    creado_en           TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_picking_lineas_orden ON inv_picking_lineas(orden_picking_id);
