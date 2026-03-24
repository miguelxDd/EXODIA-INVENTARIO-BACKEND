CREATE TABLE inv_recepciones (
    id                   BIGSERIAL PRIMARY KEY,
    empresa_id           BIGINT NOT NULL REFERENCES inv_empresas(id),
    numero_recepcion     VARCHAR(50) NOT NULL,
    bodega_id            BIGINT NOT NULL REFERENCES inv_bodegas(id),
    tipo_recepcion       VARCHAR(30) NOT NULL,
    referencia_origen_id BIGINT,
    proveedor_id         BIGINT,
    estado               VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADO',
    comentarios          TEXT,
    creado_en            TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por           BIGINT,
    modificado_en        TIMESTAMPTZ NOT NULL DEFAULT now(),
    modificado_por       BIGINT,
    activo               BOOLEAN NOT NULL DEFAULT true,
    version              BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_recepciones_empresa ON inv_recepciones(empresa_id);

CREATE TABLE inv_recepcion_lineas (
    id                  BIGSERIAL PRIMARY KEY,
    recepcion_id        BIGINT NOT NULL REFERENCES inv_recepciones(id),
    contenedor_id       BIGINT NOT NULL REFERENCES inv_contenedores(id),
    producto_id         BIGINT NOT NULL,
    unidad_id           BIGINT NOT NULL REFERENCES inv_unidades(id),
    ubicacion_id        BIGINT NOT NULL REFERENCES inv_ubicaciones(id),
    cantidad            NUMERIC(18,6) NOT NULL,
    precio_unitario     NUMERIC(18,6) NOT NULL DEFAULT 0,
    numero_lote         VARCHAR(100),
    fecha_vencimiento   DATE,
    barcode_generado    BOOLEAN NOT NULL DEFAULT false,
    barcode_reutilizado BOOLEAN NOT NULL DEFAULT false,
    operacion_id        BIGINT REFERENCES inv_operaciones(id),
    creado_en           TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_recepcion_lineas_recepcion ON inv_recepcion_lineas(recepcion_id);
