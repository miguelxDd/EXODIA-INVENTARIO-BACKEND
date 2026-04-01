CREATE TABLE inv_picking_linea_asignaciones (
    id                  BIGSERIAL PRIMARY KEY,
    picking_linea_id    BIGINT NOT NULL REFERENCES inv_picking_lineas(id),
    contenedor_id       BIGINT NOT NULL REFERENCES inv_contenedores(id),
    operacion_id        BIGINT NOT NULL REFERENCES inv_operaciones(id),
    cantidad_pickeada   NUMERIC(18,6) NOT NULL,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_picking_linea_asignaciones_linea
    ON inv_picking_linea_asignaciones(picking_linea_id);

CREATE INDEX idx_picking_linea_asignaciones_operacion
    ON inv_picking_linea_asignaciones(operacion_id);
