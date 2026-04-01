CREATE TABLE inv_fotos_costo (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES inv_empresas(id),
    producto_id     BIGINT NOT NULL,
    bodega_id       BIGINT REFERENCES inv_bodegas(id),
    unidad_id       BIGINT NOT NULL REFERENCES inv_unidades(id),
    cantidad_stock  NUMERIC(18,6) NOT NULL,
    costo_unitario  NUMERIC(18,6) NOT NULL,
    costo_total     NUMERIC(18,6) NOT NULL,
    metodo_costo    VARCHAR(30) NOT NULL,
    fecha_foto      TIMESTAMPTZ NOT NULL,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_fotos_costo_empresa_producto ON inv_fotos_costo(empresa_id, producto_id);
CREATE INDEX idx_fotos_costo_fecha ON inv_fotos_costo(fecha_foto);
