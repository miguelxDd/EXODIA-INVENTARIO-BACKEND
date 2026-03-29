-- V403: Tabla para rastrear multiples operaciones de entrada por TransferenciaContenedor (recepciones parciales)
CREATE TABLE inv_transferencia_contenedor_entradas (
    id                          BIGSERIAL PRIMARY KEY,
    transferencia_contenedor_id BIGINT NOT NULL REFERENCES inv_transferencia_contenedores(id),
    operacion_entrada_id        BIGINT NOT NULL REFERENCES inv_operaciones(id),
    cantidad_recibida           NUMERIC(18,6) NOT NULL,
    contenedor_destino_id       BIGINT REFERENCES inv_contenedores(id),
    creado_en                   TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_tc_entradas_tc ON inv_transferencia_contenedor_entradas(transferencia_contenedor_id);
