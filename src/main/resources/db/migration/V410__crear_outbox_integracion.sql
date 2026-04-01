CREATE TABLE inv_eventos_outbox (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES inv_empresas(id),
    aggregate_type  VARCHAR(100) NOT NULL,
    aggregate_id    BIGINT NOT NULL,
    event_type      VARCHAR(150) NOT NULL,
    payload_json    TEXT NOT NULL,
    estado          VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    correlation_id  VARCHAR(100),
    intentos        INTEGER NOT NULL DEFAULT 0,
    publicado_en    TIMESTAMPTZ,
    ultimo_error    VARCHAR(2000),
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por      BIGINT,
    modificado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    modificado_por  BIGINT,
    activo          BOOLEAN NOT NULL DEFAULT true,
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_estado_creado_en
    ON inv_eventos_outbox (estado, creado_en);

CREATE INDEX idx_outbox_empresa_event_type
    ON inv_eventos_outbox (empresa_id, event_type);
