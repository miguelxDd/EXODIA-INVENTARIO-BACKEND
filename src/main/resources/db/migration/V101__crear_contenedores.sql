CREATE TABLE inv_contenedores (
    id                    BIGSERIAL PRIMARY KEY,
    empresa_id            BIGINT NOT NULL REFERENCES inv_empresas(id),
    codigo_barras         VARCHAR(100) NOT NULL,
    producto_id           BIGINT NOT NULL,
    proveedor_id          BIGINT,
    producto_proveedor_id BIGINT,
    unidad_id             BIGINT NOT NULL REFERENCES inv_unidades(id),
    bodega_id             BIGINT NOT NULL REFERENCES inv_bodegas(id),
    ubicacion_id          BIGINT NOT NULL REFERENCES inv_ubicaciones(id),
    precio_unitario       NUMERIC(18,6) NOT NULL DEFAULT 0,
    lote_id               BIGINT REFERENCES inv_lotes(id),
    numero_lote           VARCHAR(100),
    fecha_vencimiento     DATE,
    numero_serie          VARCHAR(100),
    marca_id              BIGINT,
    origen_id             BIGINT,
    info_garantia         VARCHAR(500),
    estado_id             BIGINT NOT NULL REFERENCES inv_estados_contenedor(id),
    creado_en             TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por            BIGINT,
    modificado_en         TIMESTAMPTZ NOT NULL DEFAULT now(),
    modificado_por        BIGINT,
    activo                BOOLEAN NOT NULL DEFAULT true,
    version               BIGINT NOT NULL DEFAULT 0
);

-- Partial unique index for barcode (soft delete compatible)
CREATE UNIQUE INDEX uq_contenedores_empresa_barcode
    ON inv_contenedores (empresa_id, codigo_barras) WHERE activo = true;

-- FK indexes
CREATE INDEX idx_contenedores_empresa ON inv_contenedores(empresa_id);
CREATE INDEX idx_contenedores_producto ON inv_contenedores(producto_id);
CREATE INDEX idx_contenedores_bodega ON inv_contenedores(bodega_id);
CREATE INDEX idx_contenedores_ubicacion ON inv_contenedores(ubicacion_id);
CREATE INDEX idx_contenedores_estado ON inv_contenedores(estado_id);
CREATE INDEX idx_contenedores_lote ON inv_contenedores(lote_id);

-- FEFO index for picking
CREATE INDEX idx_contenedores_fefo
    ON inv_contenedores(producto_id, bodega_id, fecha_vencimiento, creado_en)
    WHERE activo = true;
