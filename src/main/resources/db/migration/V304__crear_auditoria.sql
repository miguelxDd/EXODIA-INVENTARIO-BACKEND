CREATE TABLE inv_auditoria (
    id               BIGSERIAL PRIMARY KEY,
    empresa_id       BIGINT NOT NULL REFERENCES inv_empresas(id),
    entidad          VARCHAR(100) NOT NULL,
    entidad_id       BIGINT NOT NULL,
    accion           VARCHAR(50) NOT NULL,
    datos_anteriores TEXT,
    datos_nuevos     TEXT,
    usuario_id       BIGINT,
    creado_en        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_auditoria_empresa ON inv_auditoria(empresa_id);
CREATE INDEX idx_auditoria_entidad ON inv_auditoria(entidad, entidad_id);
CREATE INDEX idx_auditoria_fecha ON inv_auditoria(creado_en);
