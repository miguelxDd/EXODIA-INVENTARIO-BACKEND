CREATE TABLE inv_conteos_fisicos (
    id                BIGSERIAL PRIMARY KEY,
    empresa_id        BIGINT NOT NULL REFERENCES inv_empresas(id),
    numero_conteo     VARCHAR(50) NOT NULL,
    bodega_id         BIGINT NOT NULL REFERENCES inv_bodegas(id),
    estado            VARCHAR(20) NOT NULL DEFAULT 'EN_PROGRESO',
    fecha_conteo      TIMESTAMPTZ NOT NULL DEFAULT now(),
    comentarios       TEXT,
    ajuste_generado_id BIGINT REFERENCES inv_ajustes(id),
    creado_en         TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por        BIGINT,
    modificado_en     TIMESTAMPTZ NOT NULL DEFAULT now(),
    modificado_por    BIGINT,
    activo            BOOLEAN NOT NULL DEFAULT true,
    version           BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_conteos_fisicos_empresa ON inv_conteos_fisicos(empresa_id);

CREATE TABLE inv_conteo_lineas (
    id               BIGSERIAL PRIMARY KEY,
    conteo_fisico_id BIGINT NOT NULL REFERENCES inv_conteos_fisicos(id),
    contenedor_id    BIGINT NOT NULL REFERENCES inv_contenedores(id),
    cantidad_sistema NUMERIC(18,6) NOT NULL,
    cantidad_contada NUMERIC(18,6),
    diferencia       NUMERIC(18,6),
    aplicado         BOOLEAN NOT NULL DEFAULT false,
    creado_en        TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_conteo_lineas_conteo ON inv_conteo_lineas(conteo_fisico_id);
