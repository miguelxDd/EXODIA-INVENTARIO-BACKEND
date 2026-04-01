CREATE TABLE inv_secuencias_barcode (
    id               BIGSERIAL PRIMARY KEY,
    empresa_id       BIGINT NOT NULL REFERENCES inv_empresas(id),
    prefijo          VARCHAR(20) NOT NULL,
    ultimo_valor     BIGINT NOT NULL DEFAULT 0,
    longitud_padding INTEGER NOT NULL DEFAULT 8,
    version          BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_secuencias_empresa_prefijo UNIQUE (empresa_id, prefijo)
);
