CREATE TABLE inv_configuracion_empresa (
    id                          BIGSERIAL PRIMARY KEY,
    empresa_id                  BIGINT NOT NULL REFERENCES inv_empresas(id),
    expiracion_reserva_horas    INTEGER NOT NULL DEFAULT 48,
    dias_alerta_vencimiento     INTEGER NOT NULL DEFAULT 90,
    barcode_prefijo             VARCHAR(20) NOT NULL DEFAULT 'INV',
    barcode_longitud_padding    INTEGER NOT NULL DEFAULT 8,
    politica_salida             VARCHAR(20) NOT NULL DEFAULT 'FEFO',
    creado_en                   TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por                  BIGINT,
    modificado_en               TIMESTAMPTZ NOT NULL DEFAULT now(),
    modificado_por              BIGINT,
    activo                      BOOLEAN NOT NULL DEFAULT true,
    version                     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_config_empresa UNIQUE (empresa_id)
);
