CREATE TABLE inv_transferencias (
    id                     BIGSERIAL PRIMARY KEY,
    empresa_id             BIGINT NOT NULL REFERENCES inv_empresas(id),
    numero_transferencia   VARCHAR(50) NOT NULL,
    tipo_transferencia     VARCHAR(30) NOT NULL,
    bodega_origen_id       BIGINT NOT NULL REFERENCES inv_bodegas(id),
    bodega_destino_id      BIGINT NOT NULL REFERENCES inv_bodegas(id),
    estado_id              BIGINT NOT NULL REFERENCES inv_estados_transferencia(id),
    comentarios            TEXT,
    fecha_despacho         TIMESTAMPTZ,
    fecha_recepcion        TIMESTAMPTZ,
    creado_en              TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por             BIGINT,
    modificado_en          TIMESTAMPTZ NOT NULL DEFAULT now(),
    modificado_por         BIGINT,
    activo                 BOOLEAN NOT NULL DEFAULT true,
    version                BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_transferencias_empresa ON inv_transferencias(empresa_id);

CREATE TABLE inv_transferencia_lineas (
    id                    BIGSERIAL PRIMARY KEY,
    transferencia_id      BIGINT NOT NULL REFERENCES inv_transferencias(id),
    producto_id           BIGINT NOT NULL,
    unidad_id             BIGINT NOT NULL REFERENCES inv_unidades(id),
    cantidad_solicitada   NUMERIC(18,6) NOT NULL,
    cantidad_despachada   NUMERIC(18,6) DEFAULT 0,
    cantidad_recibida     NUMERIC(18,6) DEFAULT 0,
    creado_en             TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_transferencia_lineas_transferencia ON inv_transferencia_lineas(transferencia_id);

CREATE TABLE inv_transferencia_contenedores (
    id                   BIGSERIAL PRIMARY KEY,
    transferencia_id     BIGINT NOT NULL REFERENCES inv_transferencias(id),
    contenedor_id        BIGINT NOT NULL REFERENCES inv_contenedores(id),
    cantidad             NUMERIC(18,6) NOT NULL,
    recibido             BOOLEAN NOT NULL DEFAULT false,
    operacion_salida_id  BIGINT REFERENCES inv_operaciones(id),
    operacion_entrada_id BIGINT REFERENCES inv_operaciones(id),
    creado_en            TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_transferencia_contenedores_transferencia ON inv_transferencia_contenedores(transferencia_id);
