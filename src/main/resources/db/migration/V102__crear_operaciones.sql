CREATE TABLE inv_operaciones (
    id                  BIGSERIAL PRIMARY KEY,
    empresa_id          BIGINT NOT NULL REFERENCES inv_empresas(id),
    contenedor_id       BIGINT NOT NULL REFERENCES inv_contenedores(id),
    codigo_barras       VARCHAR(100) NOT NULL,
    producto_id         BIGINT NOT NULL,
    bodega_id           BIGINT NOT NULL REFERENCES inv_bodegas(id),
    ubicacion_id        BIGINT NOT NULL REFERENCES inv_ubicaciones(id),
    unidad_id           BIGINT NOT NULL REFERENCES inv_unidades(id),
    tipo_operacion_id   BIGINT NOT NULL REFERENCES inv_tipos_operacion(id),
    cantidad            NUMERIC(18,6) NOT NULL,
    precio_unitario     NUMERIC(18,6) NOT NULL DEFAULT 0,
    tipo_ajuste_id      BIGINT REFERENCES inv_tipos_ajuste(id),
    numero_lote         VARCHAR(100),
    fecha_vencimiento   DATE,
    proveedor_id        BIGINT,
    tipo_referencia     VARCHAR(50),
    referencia_id       BIGINT,
    referencia_linea_id BIGINT,
    comentarios         TEXT,
    fecha_operacion     TIMESTAMPTZ NOT NULL DEFAULT now(),
    activo              BOOLEAN NOT NULL DEFAULT true,
    creado_por          BIGINT,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Critical stock query indexes
CREATE INDEX idx_operaciones_contenedor_activo ON inv_operaciones(contenedor_id, activo);
CREATE INDEX idx_operaciones_producto_bodega ON inv_operaciones(producto_id, bodega_id, empresa_id, activo);
CREATE INDEX idx_operaciones_empresa ON inv_operaciones(empresa_id);
CREATE INDEX idx_operaciones_kardex ON inv_operaciones(empresa_id, fecha_operacion DESC, id DESC);
CREATE INDEX idx_operaciones_barcode ON inv_operaciones(empresa_id, codigo_barras, activo);
CREATE INDEX idx_operaciones_referencia ON inv_operaciones(tipo_referencia, referencia_id);
