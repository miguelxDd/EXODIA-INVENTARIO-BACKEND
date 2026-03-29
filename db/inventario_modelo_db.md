# Modelo de Base de Datos — Módulo de Inventario

**Stack:** Java 21 + Spring Boot 4.0.x + PostgreSQL 18 + Angular 21
**Convenciones:** snake_case para tablas/columnas, prefijo `inv_` para tablas del módulo, UUID como PK donde convenga trazabilidad externa, BIGSERIAL donde convenga rendimiento interno. BD completa en español.

---

## 1. Decisiones de diseño

### 1.1. Stock calculado, nunca persistido

No existe columna `stock` en ninguna tabla. El stock se reconstruye siempre como:

```sql
SELECT SUM(o.cantidad)
FROM inv_operaciones o
WHERE o.contenedor_id = ?
  AND o.activo = true;
```

Esto garantiza trazabilidad completa y elimina inconsistencias.

### 1.2. Contenedor como unidad trazable

Cada registro en `inv_contenedores` representa una unidad trazable con codigo_barras, producto, proveedor, unidad, bodega, ubicación, lote y vencimiento. Es la pieza central del modelo.

### 1.3. Operación como unidad atómica de movimiento

Cada cambio de stock genera un registro en `inv_operaciones`. Cantidades positivas = entradas, negativas = salidas, cero = ajustes informativos (ej. cambio de precio).

### 1.4. Catálogos con enums en Java + tabla de referencia en BD

Los tipos de operación y estados se manejan con tablas de catálogo en PostgreSQL Y enums en Java. La tabla es la fuente de verdad; el enum Java es el mapeo tipado.

### 1.5. Auditoría integrada

Todas las tablas principales llevan `creado_por`, `modificado_por`, `creado_en`, `modificado_en`. Además hay una tabla de auditoría dedicada para cambios críticos.

### 1.6. Soft delete donde aplique

Catálogos y contenedores usan `activo` en lugar de borrado físico.

### 1.7. Multi-empresa

Todas las tablas operativas llevan `empresa_id` para soportar multi-tenant por discriminador.

---

## 2. Diagrama de grupos de tablas

```
┌─────────────────────────────────────────────────────────┐
│                    CATÁLOGOS BASE                        │
│  inv_empresas                                           │
│  inv_bodegas                                            │
│  inv_ubicaciones                                        │
│  inv_unidades                                           │
│  inv_conversiones                                       │
│  inv_tipos_operacion                                    │
│  inv_tipos_ajuste                                       │
│  inv_estados_contenedor                                 │
│  inv_estados_transferencia                              │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                  NÚCLEO OPERATIVO                        │
│  inv_lotes             (catálogo maestro de lotes)      │
│  inv_contenedores      (maestro del contenedor)         │
│  inv_operaciones       (kardex de movimientos)          │
│  inv_reservas          (reservas de stock)               │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                FLUJOS DE NEGOCIO                         │
│  inv_recepciones             (cabecera de recepción)    │
│  inv_recepcion_lineas        (detalle por contenedor)   │
│  inv_transferencias          (cabecera de transferencia) │
│  inv_transferencia_lineas    (detalle por producto)     │
│  inv_transferencia_contenedores (detalle por contenedor)│
│  inv_ajustes                 (cabecera de ajuste)       │
│  inv_ajuste_lineas           (detalle por contenedor)   │
│  inv_ordenes_picking         (cabecera de picking)      │
│  inv_picking_lineas          (detalle por material)     │
│  inv_conteos_fisicos         (conteo físico)            │
│  inv_conteo_lineas           (detalle del conteo)       │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                   EXTENSIONES                            │
│  inv_maximos_minimos         (máximos y mínimos)        │
│  inv_config_merma            (configuración de merma)   │
│  inv_registros_merma         (registros de merma)       │
│  inv_fotos_costo             (fotos de costo periódicas)│
│  inv_secuencias_barcode      (secuencias de barcode)    │
│  inv_auditoria               (auditoría de cambios)    │
└─────────────────────────────────────────────────────────┘
```

---

## 3. DDL completo por tabla

### 3.1. CATÁLOGOS BASE

#### inv_empresas

```sql
CREATE TABLE inv_empresas (
    id              BIGSERIAL       PRIMARY KEY,
    codigo          VARCHAR(20)     NOT NULL UNIQUE,
    nombre          VARCHAR(200)    NOT NULL,
    nit             VARCHAR(50),
    activo          BOOLEAN         NOT NULL DEFAULT true,
    version         BIGINT          NOT NULL DEFAULT 0,
    creado_en       TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en   TIMESTAMPTZ     NOT NULL DEFAULT now()
);

COMMENT ON TABLE inv_empresas IS 'Empresas o tenants del sistema';
```

---

#### inv_bodegas

```sql
CREATE TABLE inv_bodegas (
    id                          BIGSERIAL       PRIMARY KEY,
    empresa_id                  BIGINT          NOT NULL REFERENCES inv_empresas(id),
    codigo                      VARCHAR(30)     NOT NULL,
    nombre                      VARCHAR(200)    NOT NULL,
    direccion                   VARCHAR(500),
    ciudad                      VARCHAR(100),
    pais                        VARCHAR(100),
    ubicacion_standby_id        BIGINT,         -- se llena después de crear la ubicación standby
    es_producto_terminado       BOOLEAN         NOT NULL DEFAULT false,
    es_consignacion             BOOLEAN         NOT NULL DEFAULT false,
    activo                      BOOLEAN         NOT NULL DEFAULT true,
    version                     BIGINT          NOT NULL DEFAULT 0,
    creado_por                  BIGINT,
    modificado_por              BIGINT,
    creado_en                   TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en               TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_bodega_codigo_empresa UNIQUE (empresa_id, codigo)
);

CREATE INDEX idx_bodega_empresa ON inv_bodegas(empresa_id) WHERE activo = true;

COMMENT ON TABLE inv_bodegas IS 'Bodegas o almacenes físicos y lógicos';
COMMENT ON COLUMN inv_bodegas.ubicacion_standby_id IS 'Ubicación standby auto-generada al crear la bodega';
```

---

#### inv_ubicaciones

```sql
CREATE TABLE inv_ubicaciones (
    id              BIGSERIAL       PRIMARY KEY,
    bodega_id       BIGINT          NOT NULL REFERENCES inv_bodegas(id),
    codigo          VARCHAR(50)     NOT NULL,
    nombre          VARCHAR(200)    NOT NULL,
    codigo_barras   VARCHAR(100),
    tipo_ubicacion  VARCHAR(30)     NOT NULL DEFAULT 'GENERAL'
                    CHECK (tipo_ubicacion IN ('GENERAL','STANDBY','TEMPORAL','RECEPCION','PRODUCCION')),
    activo          BOOLEAN         NOT NULL DEFAULT true,
    version         BIGINT          NOT NULL DEFAULT 0,
    creado_por      BIGINT,
    modificado_por  BIGINT,
    creado_en       TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en   TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_ubicacion_codigo_bodega UNIQUE (bodega_id, codigo)
);

CREATE INDEX idx_ubicacion_bodega ON inv_ubicaciones(bodega_id) WHERE activo = true;
CREATE INDEX idx_ubicacion_codigo_barras ON inv_ubicaciones(codigo_barras) WHERE codigo_barras IS NOT NULL;

COMMENT ON TABLE inv_ubicaciones IS 'Ubicaciones físicas dentro de cada bodega';
COMMENT ON COLUMN inv_ubicaciones.tipo_ubicacion IS 'Tipo: GENERAL, STANDBY, TEMPORAL, RECEPCION, PRODUCCION';
```

Ahora la FK diferida de inv_bodegas:

```sql
ALTER TABLE inv_bodegas
    ADD CONSTRAINT fk_bodega_ubicacion_standby
    FOREIGN KEY (ubicacion_standby_id) REFERENCES inv_ubicaciones(id);
```

---

#### inv_unidades

```sql
CREATE TABLE inv_unidades (
    id              BIGSERIAL       PRIMARY KEY,
    empresa_id      BIGINT          NOT NULL REFERENCES inv_empresas(id),
    codigo          VARCHAR(20)     NOT NULL,
    nombre          VARCHAR(100)    NOT NULL,
    abreviatura     VARCHAR(10),
    activo          BOOLEAN         NOT NULL DEFAULT true,
    version         BIGINT          NOT NULL DEFAULT 0,
    creado_en       TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en   TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_unidad_codigo_empresa UNIQUE (empresa_id, codigo)
);

COMMENT ON TABLE inv_unidades IS 'Unidades de medida: Unidad, Caja, Docena, Libra, Kg, Galón, etc.';
```

---

#### inv_conversiones

```sql
CREATE TABLE inv_conversiones (
    id                  BIGSERIAL       PRIMARY KEY,
    empresa_id          BIGINT          NOT NULL REFERENCES inv_empresas(id),
    unidad_origen_id    BIGINT          NOT NULL REFERENCES inv_unidades(id),
    unidad_destino_id   BIGINT          NOT NULL REFERENCES inv_unidades(id),
    factor_conversion   NUMERIC(18,6)   NOT NULL,
    tipo_operacion      VARCHAR(15)     NOT NULL CHECK (tipo_operacion IN ('MULTIPLICAR','DIVIDIR')),
    producto_id         BIGINT,         -- NULL = conversión universal, con valor = específica del producto
    activo              BOOLEAN         NOT NULL DEFAULT true,
    version             BIGINT          NOT NULL DEFAULT 0,
    creado_en           TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en       TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_conversion UNIQUE (empresa_id, unidad_origen_id, unidad_destino_id, producto_id)
);

COMMENT ON TABLE inv_conversiones IS 'Reglas de conversión entre unidades. producto_id NULL = universal';
COMMENT ON COLUMN inv_conversiones.tipo_operacion IS 'MULTIPLICAR: destino = origen * factor. DIVIDIR: destino = origen / factor';
```

---

#### inv_tipos_operacion

```sql
CREATE TABLE inv_tipos_operacion (
    id              BIGSERIAL       PRIMARY KEY,
    codigo          VARCHAR(30)     NOT NULL UNIQUE,
    nombre          VARCHAR(100)    NOT NULL,
    signo           SMALLINT        NOT NULL CHECK (signo IN (-1, 0, 1)),
    descripcion     VARCHAR(500),
    activo          BOOLEAN         NOT NULL DEFAULT true
);

-- Datos iniciales
INSERT INTO inv_tipos_operacion (codigo, nombre, signo, descripcion) VALUES
    ('RECEPCION',               'Recepción',                     1,  'Entrada por recepción manual, compra o traslado'),
    ('PICKING',                 'Picking',                      -1,  'Salida por picking/requisición'),
    ('AJUSTE_POSITIVO',         'Ajuste positivo',               1,  'Ajuste que incrementa stock'),
    ('AJUSTE_NEGATIVO',         'Ajuste negativo',              -1,  'Ajuste que disminuye stock'),
    ('AJUSTE_INFORMATIVO',      'Ajuste informativo',            0,  'Cambio de precio u otro dato sin mover stock'),
    ('SALIDA_TRANSFERENCIA',    'Salida por transferencia',     -1,  'Salida de bodega origen en traslado'),
    ('ENTRADA_TRANSFERENCIA',   'Entrada por transferencia',     1,  'Entrada en bodega destino en traslado'),
    ('SALIDA_MOVIMIENTO',       'Salida por movimiento',        -1,  'Salida de ubicación por movimiento interno'),
    ('ENTRADA_MOVIMIENTO',      'Entrada por movimiento',        1,  'Entrada a ubicación por movimiento interno'),
    ('SALIDA_CONVERSION',       'Salida por conversión',        -1,  'Salida de unidad origen en conversión'),
    ('ENTRADA_CONVERSION',      'Entrada por conversión',        1,  'Entrada en unidad destino en conversión'),
    ('INGRESO_PRODUCCION',      'Ingreso por producción',        1,  'Entrada de producto terminado'),
    ('MERMA',                   'Merma',                        -1,  'Salida por merma/pérdida'),
    ('AJUSTE_VENTA',            'Ajuste por venta',             -1,  'Descuento contra venta facturada'),
    ('CONTEO_POSITIVO',         'Ajuste conteo positivo',        1,  'Sobrante detectado en conteo físico'),
    ('CONTEO_NEGATIVO',         'Ajuste conteo negativo',       -1,  'Faltante detectado en conteo físico');

COMMENT ON TABLE inv_tipos_operacion IS 'Catálogo de tipos de operación. signo: 1=entrada, -1=salida, 0=informativo';
```

---

#### inv_tipos_ajuste

```sql
CREATE TABLE inv_tipos_ajuste (
    id              BIGSERIAL       PRIMARY KEY,
    codigo          VARCHAR(30)     NOT NULL UNIQUE,
    nombre          VARCHAR(100)    NOT NULL,
    requiere_doc    BOOLEAN         NOT NULL DEFAULT false,
    activo          BOOLEAN         NOT NULL DEFAULT true
);

INSERT INTO inv_tipos_ajuste (codigo, nombre, requiere_doc) VALUES
    ('DANO',                'Daño/rotura',              false),
    ('VENCIMIENTO',         'Vencimiento',              false),
    ('ROBO',                'Robo/hurto',               true),
    ('DIFERENCIA_CONTEO',   'Diferencia de conteo',     true),
    ('DONACION',            'Donación',                 true),
    ('DEVOLUCION_PROVEEDOR','Devolución a proveedor',   true),
    ('RECLASIFICACION',     'Reclasificación',          false),
    ('CAMBIO_PRECIO',       'Cambio de precio',         false),
    ('OTRO',                'Otro',                     false);

COMMENT ON TABLE inv_tipos_ajuste IS 'Razones o motivos de ajuste de inventario';
```

---

#### inv_estados_contenedor

```sql
CREATE TABLE inv_estados_contenedor (
    id                  BIGSERIAL       PRIMARY KEY,
    codigo              VARCHAR(30)     NOT NULL UNIQUE,
    nombre              VARCHAR(100)    NOT NULL,
    permite_picking     BOOLEAN         NOT NULL DEFAULT false,
    permite_traslado    BOOLEAN         NOT NULL DEFAULT false
);

INSERT INTO inv_estados_contenedor (codigo, nombre, permite_picking, permite_traslado) VALUES
    ('DISPONIBLE',      'Disponible',       true,   true),
    ('RESERVADO',       'Reservado',        false,  false),
    ('EN_TRANSITO',     'En tránsito',      false,  false),
    ('EN_STANDBY',      'En standby',       false,  false),
    ('CUARENTENA',      'En cuarentena',    false,  false),
    ('BLOQUEADO',       'Bloqueado',        false,  false),
    ('AGOTADO',         'Agotado',          false,  false);

COMMENT ON TABLE inv_estados_contenedor IS 'Estados del ciclo de vida del contenedor';
```

---

#### inv_estados_transferencia

```sql
CREATE TABLE inv_estados_transferencia (
    id              BIGSERIAL       PRIMARY KEY,
    codigo          VARCHAR(30)     NOT NULL UNIQUE,
    nombre          VARCHAR(100)    NOT NULL,
    orden           INT             NOT NULL DEFAULT 0
);

INSERT INTO inv_estados_transferencia (codigo, nombre, orden) VALUES
    ('BORRADOR',            'Borrador',             1),
    ('CONFIRMADO',          'Confirmado',           2),
    ('DESPACHADO',          'Despachado',           3),
    ('EN_TRANSITO',         'En tránsito',          4),
    ('RECIBIDO_PARCIAL',    'Recibido parcial',     5),
    ('RECIBIDO_COMPLETO',   'Recibido completo',    6),
    ('CANCELADO',           'Cancelado',            7),
    ('CIERRE_FORZADO',      'Cierre forzado',       8);

COMMENT ON TABLE inv_estados_transferencia IS 'Estados del flujo de transferencia entre bodegas';
```

---

### 3.2. NÚCLEO OPERATIVO

#### inv_lotes

```sql
CREATE TABLE inv_lotes (
    id                  BIGSERIAL       PRIMARY KEY,
    empresa_id          BIGINT          NOT NULL REFERENCES inv_empresas(id),
    numero_lote         VARCHAR(100)    NOT NULL,
    producto_id         BIGINT,         -- FK a tabla de productos (módulo externo)
    proveedor_id        BIGINT,         -- FK a tabla de proveedores (módulo externo)
    fecha_vencimiento   DATE,
    fecha_fabricacion   DATE,
    estado              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVO'
                        CHECK (estado IN ('ACTIVO','CUARENTENA','VENCIDO','BLOQUEADO','CONSUMIDO')),
    notas               TEXT,
    version             BIGINT          NOT NULL DEFAULT 0,
    creado_en           TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en       TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_numero_lote_empresa UNIQUE (empresa_id, numero_lote)
);

CREATE INDEX idx_lote_vencimiento ON inv_lotes(fecha_vencimiento) WHERE estado = 'ACTIVO';
CREATE INDEX idx_lote_producto ON inv_lotes(producto_id);

COMMENT ON TABLE inv_lotes IS 'Catálogo maestro de lotes con trazabilidad completa';
```

---

#### inv_contenedores

```sql
CREATE TABLE inv_contenedores (
    id                          BIGSERIAL       PRIMARY KEY,
    empresa_id                  BIGINT          NOT NULL REFERENCES inv_empresas(id),
    codigo_barras               VARCHAR(100)    NOT NULL,
    producto_id                 BIGINT          NOT NULL,       -- FK externa a productos
    proveedor_id                BIGINT,                         -- FK externa a proveedores
    producto_proveedor_id       BIGINT,                         -- FK externa a relación producto-proveedor
    unidad_id                   BIGINT          NOT NULL REFERENCES inv_unidades(id),
    bodega_id                   BIGINT          NOT NULL REFERENCES inv_bodegas(id),
    ubicacion_id                BIGINT          NOT NULL REFERENCES inv_ubicaciones(id),
    precio_unitario             NUMERIC(18,6)   NOT NULL DEFAULT 0,
    lote_id                     BIGINT          REFERENCES inv_lotes(id),
    numero_lote                 VARCHAR(100),
    fecha_vencimiento           DATE,
    numero_serie                VARCHAR(100),
    marca_id                    BIGINT,         -- FK externa si aplica
    origen_id                   BIGINT,         -- FK externa si aplica
    info_garantia               VARCHAR(500),
    estado_id                   BIGINT          NOT NULL REFERENCES inv_estados_contenedor(id),
    activo                      BOOLEAN         NOT NULL DEFAULT true,
    version                     BIGINT          NOT NULL DEFAULT 0,
    creado_por                  BIGINT,
    modificado_por              BIGINT,
    creado_en                   TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en               TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- Partial unique index: permite reutilizar codigo_barras de contenedores soft-deleted
CREATE UNIQUE INDEX uq_contenedor_empresa_codigo_barras
    ON inv_contenedores (empresa_id, codigo_barras) WHERE activo = true;

CREATE INDEX idx_contenedor_producto ON inv_contenedores(producto_id);
CREATE INDEX idx_contenedor_bodega ON inv_contenedores(bodega_id);
CREATE INDEX idx_contenedor_ubicacion ON inv_contenedores(ubicacion_id);
CREATE INDEX idx_contenedor_codigo_barras ON inv_contenedores(codigo_barras);
CREATE INDEX idx_contenedor_lote ON inv_contenedores(lote_id) WHERE lote_id IS NOT NULL;
CREATE INDEX idx_contenedor_proveedor ON inv_contenedores(proveedor_id) WHERE proveedor_id IS NOT NULL;
CREATE INDEX idx_contenedor_vencimiento ON inv_contenedores(fecha_vencimiento) WHERE fecha_vencimiento IS NOT NULL;
CREATE INDEX idx_contenedor_estado ON inv_contenedores(estado_id);
CREATE INDEX idx_contenedor_compuesto_stock ON inv_contenedores(empresa_id, producto_id, bodega_id, activo);
-- Índice FEFO: picking ordenado por vencimiento y antigüedad
CREATE INDEX idx_contenedor_fefo ON inv_contenedores(producto_id, bodega_id, fecha_vencimiento, creado_en) WHERE activo = true;

COMMENT ON TABLE inv_contenedores IS 'Maestro del contenedor: unidad trazable del inventario por código de barras';
```

---

#### inv_operaciones

```sql
CREATE TABLE inv_operaciones (
    id                          BIGSERIAL       PRIMARY KEY,
    empresa_id                  BIGINT          NOT NULL REFERENCES inv_empresas(id),
    contenedor_id               BIGINT          NOT NULL REFERENCES inv_contenedores(id),
    codigo_barras               VARCHAR(100)    NOT NULL,       -- desnormalizado para consultas rápidas
    producto_id                 BIGINT          NOT NULL,       -- desnormalizado
    bodega_id                   BIGINT          NOT NULL REFERENCES inv_bodegas(id),
    ubicacion_id                BIGINT          NOT NULL REFERENCES inv_ubicaciones(id),
    unidad_id                   BIGINT          NOT NULL REFERENCES inv_unidades(id),
    tipo_operacion_id           BIGINT          NOT NULL REFERENCES inv_tipos_operacion(id),
    cantidad                    NUMERIC(18,6)   NOT NULL,       -- positivo=entrada, negativo=salida, 0=informativo
    precio_unitario             NUMERIC(18,6)   NOT NULL DEFAULT 0,
    tipo_ajuste_id              BIGINT          REFERENCES inv_tipos_ajuste(id),
    numero_lote                 VARCHAR(100),
    fecha_vencimiento           DATE,
    proveedor_id                BIGINT,
    -- Referencias cruzadas a documentos de origen
    tipo_referencia             VARCHAR(50),    -- 'RECEPCION', 'TRANSFERENCIA', 'PICKING', 'AJUSTE', 'ORDEN_COMPRA', 'VENTA', 'ORDEN_PRODUCCION', 'CONTEO_FISICO'
    referencia_id               BIGINT,         -- ID del documento de origen
    referencia_linea_id         BIGINT,         -- ID de la línea del documento
    comentarios                 TEXT,
    fecha_operacion             TIMESTAMPTZ     NOT NULL DEFAULT now(),
    activo                      BOOLEAN         NOT NULL DEFAULT true,
    creado_por                  BIGINT,
    creado_en                   TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- Nota: inv_operaciones NO lleva version ni modificado_en/por porque es inmutable

-- Índices críticos para rendimiento de consultas de stock
CREATE INDEX idx_operacion_contenedor ON inv_operaciones(contenedor_id) WHERE activo = true;
CREATE INDEX idx_operacion_codigo_barras ON inv_operaciones(codigo_barras) WHERE activo = true;
CREATE INDEX idx_operacion_producto_bodega ON inv_operaciones(producto_id, bodega_id) WHERE activo = true;
CREATE INDEX idx_operacion_producto ON inv_operaciones(producto_id) WHERE activo = true;
CREATE INDEX idx_operacion_bodega ON inv_operaciones(bodega_id) WHERE activo = true;
CREATE INDEX idx_operacion_tipo ON inv_operaciones(tipo_operacion_id);
CREATE INDEX idx_operacion_fecha ON inv_operaciones(fecha_operacion);
CREATE INDEX idx_operacion_referencia ON inv_operaciones(tipo_referencia, referencia_id) WHERE tipo_referencia IS NOT NULL;
-- Índice de kardex con id DESC para paginación correcta
CREATE INDEX idx_operacion_kardex ON inv_operaciones(empresa_id, fecha_operacion DESC, id DESC);

-- Índice compuesto para la consulta más frecuente: stock por codigo_barras
CREATE INDEX idx_operacion_stock_por_codigo_barras ON inv_operaciones(empresa_id, codigo_barras, activo) INCLUDE (cantidad);

-- Índice compuesto para stock por producto+bodega
CREATE INDEX idx_operacion_stock_por_producto_bodega ON inv_operaciones(empresa_id, producto_id, bodega_id, activo) INCLUDE (cantidad);

COMMENT ON TABLE inv_operaciones IS 'Kardex: cada fila es un movimiento atómico de inventario. Stock = SUM(cantidad). Inmutable.';
COMMENT ON COLUMN inv_operaciones.cantidad IS '+entrada, -salida, 0=informativo (ej. cambio de precio)';
COMMENT ON COLUMN inv_operaciones.tipo_referencia IS 'Tipo de documento origen: RECEPCION, TRANSFERENCIA, PICKING, etc.';
```

---

#### inv_reservas

```sql
CREATE TABLE inv_reservas (
    id                      BIGSERIAL       PRIMARY KEY,
    empresa_id              BIGINT          NOT NULL REFERENCES inv_empresas(id),
    contenedor_id           BIGINT          NOT NULL REFERENCES inv_contenedores(id),
    codigo_barras           VARCHAR(100)    NOT NULL,
    producto_id             BIGINT          NOT NULL,
    bodega_id               BIGINT          NOT NULL REFERENCES inv_bodegas(id),
    cantidad_reservada      NUMERIC(18,6)   NOT NULL CHECK (cantidad_reservada > 0),
    cantidad_despachada     NUMERIC(18,6)   NOT NULL DEFAULT 0,
    estado                  VARCHAR(20)     NOT NULL DEFAULT 'PENDIENTE'
                            CHECK (estado IN ('PENDIENTE','PARCIAL','COMPLETADA','CANCELADA','EXPIRADA')),
    tipo_referencia         VARCHAR(50)     NOT NULL,   -- 'PICKING', 'TRANSFERENCIA', 'ORDEN_VENTA'
    referencia_id           BIGINT          NOT NULL,
    referencia_linea_id     BIGINT,
    expira_en               TIMESTAMPTZ,
    version                 BIGINT          NOT NULL DEFAULT 0,
    creado_por              BIGINT,
    creado_en               TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en           TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX idx_reserva_contenedor ON inv_reservas(contenedor_id) WHERE estado = 'PENDIENTE';
CREATE INDEX idx_reserva_producto ON inv_reservas(producto_id, bodega_id) WHERE estado IN ('PENDIENTE','PARCIAL');
CREATE INDEX idx_reserva_referencia ON inv_reservas(tipo_referencia, referencia_id);

COMMENT ON TABLE inv_reservas IS 'Reservas de stock que inmovilizan cantidad sin descontarla aún';
COMMENT ON COLUMN inv_reservas.cantidad_reservada IS 'Cantidad reservada original';
COMMENT ON COLUMN inv_reservas.cantidad_despachada IS 'Cantidad ya despachada/pickeada contra esta reserva';
```

---

### 3.3. FLUJOS DE NEGOCIO

#### inv_recepciones (cabecera de recepción)

```sql
CREATE TABLE inv_recepciones (
    id                      BIGSERIAL       PRIMARY KEY,
    empresa_id              BIGINT          NOT NULL REFERENCES inv_empresas(id),
    numero_recepcion        VARCHAR(30)     NOT NULL,
    bodega_id               BIGINT          NOT NULL REFERENCES inv_bodegas(id),
    tipo_recepcion          VARCHAR(30)     NOT NULL DEFAULT 'MANUAL'
                            CHECK (tipo_recepcion IN ('MANUAL','ORDEN_COMPRA','TRANSFERENCIA','PRODUCCION','DEVOLUCION')),
    referencia_origen_id    BIGINT,         -- ID de orden de compra, traslado, etc.
    proveedor_id            BIGINT,
    fecha_recepcion         TIMESTAMPTZ     NOT NULL DEFAULT now(),
    estado                  VARCHAR(20)     NOT NULL DEFAULT 'BORRADOR'
                            CHECK (estado IN ('BORRADOR','CONFIRMADA','CANCELADA')),
    comentarios             TEXT,
    version                 BIGINT          NOT NULL DEFAULT 0,
    creado_por              BIGINT,
    modificado_por          BIGINT,
    creado_en               TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en           TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_numero_recepcion UNIQUE (empresa_id, numero_recepcion)
);

COMMENT ON TABLE inv_recepciones IS 'Cabecera de recepción de inventario';
```

---

#### inv_recepcion_lineas

```sql
CREATE TABLE inv_recepcion_lineas (
    id                      BIGSERIAL       PRIMARY KEY,
    recepcion_id            BIGINT          NOT NULL REFERENCES inv_recepciones(id),
    contenedor_id           BIGINT          NOT NULL REFERENCES inv_contenedores(id),
    producto_id             BIGINT          NOT NULL,
    unidad_id               BIGINT          NOT NULL REFERENCES inv_unidades(id),
    ubicacion_id            BIGINT          NOT NULL REFERENCES inv_ubicaciones(id),
    cantidad                NUMERIC(18,6)   NOT NULL CHECK (cantidad > 0),
    precio_unitario         NUMERIC(18,6)   NOT NULL DEFAULT 0,
    numero_lote             VARCHAR(100),
    fecha_vencimiento       DATE,
    barcode_generado        BOOLEAN         NOT NULL DEFAULT false,
    barcode_reutilizado     BOOLEAN         NOT NULL DEFAULT false,
    operacion_id            BIGINT          REFERENCES inv_operaciones(id),
    creado_en               TIMESTAMPTZ     NOT NULL DEFAULT now()
);

COMMENT ON TABLE inv_recepcion_lineas IS 'Líneas de recepción: un registro por contenedor recibido';
```

---

#### inv_transferencias (cabecera de transferencia)

```sql
CREATE TABLE inv_transferencias (
    id                          BIGSERIAL       PRIMARY KEY,
    empresa_id                  BIGINT          NOT NULL REFERENCES inv_empresas(id),
    numero_transferencia        VARCHAR(30)     NOT NULL,
    tipo_transferencia          VARCHAR(20)     NOT NULL CHECK (tipo_transferencia IN ('POR_CONTENEDOR','POR_PRODUCTO')),
    bodega_origen_id            BIGINT          NOT NULL REFERENCES inv_bodegas(id),
    bodega_destino_id           BIGINT          NOT NULL REFERENCES inv_bodegas(id),
    estado_id                   BIGINT          NOT NULL REFERENCES inv_estados_transferencia(id),
    fecha_transferencia         TIMESTAMPTZ     NOT NULL DEFAULT now(),
    fecha_despacho              TIMESTAMPTZ,
    fecha_recepcion             TIMESTAMPTZ,
    comentarios                 TEXT,
    version                     BIGINT          NOT NULL DEFAULT 0,
    creado_por                  BIGINT,
    modificado_por              BIGINT,
    creado_en                   TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en               TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_numero_transferencia UNIQUE (empresa_id, numero_transferencia),
    CONSTRAINT chk_bodegas_diferentes CHECK (bodega_origen_id <> bodega_destino_id)
);

CREATE INDEX idx_transferencia_estado ON inv_transferencias(estado_id);
CREATE INDEX idx_transferencia_origen ON inv_transferencias(bodega_origen_id);
CREATE INDEX idx_transferencia_destino ON inv_transferencias(bodega_destino_id);

COMMENT ON TABLE inv_transferencias IS 'Cabecera de transferencia entre bodegas. Soporta dos modalidades.';
```

---

#### inv_transferencia_lineas (detalle por producto -- traslado por producto)

```sql
CREATE TABLE inv_transferencia_lineas (
    id                      BIGSERIAL       PRIMARY KEY,
    transferencia_id        BIGINT          NOT NULL REFERENCES inv_transferencias(id),
    producto_id             BIGINT          NOT NULL,
    unidad_id               BIGINT          NOT NULL REFERENCES inv_unidades(id),
    cantidad_solicitada     NUMERIC(18,6)   NOT NULL CHECK (cantidad_solicitada > 0),
    cantidad_despachada     NUMERIC(18,6)   NOT NULL DEFAULT 0,
    cantidad_recibida       NUMERIC(18,6)   NOT NULL DEFAULT 0,
    recibido                BOOLEAN         NOT NULL DEFAULT false,
    version                 BIGINT          NOT NULL DEFAULT 0,
    creado_en               TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en           TIMESTAMPTZ     NOT NULL DEFAULT now()
);

COMMENT ON TABLE inv_transferencia_lineas IS 'Líneas de transferencia por producto (modalidad POR_PRODUCTO)';
```

---

#### inv_transferencia_contenedores (detalle por contenedor -- traslado por contenedor)

```sql
CREATE TABLE inv_transferencia_contenedores (
    id                      BIGSERIAL       PRIMARY KEY,
    transferencia_id        BIGINT          NOT NULL REFERENCES inv_transferencias(id),
    contenedor_id           BIGINT          NOT NULL REFERENCES inv_contenedores(id),
    codigo_barras           VARCHAR(100)    NOT NULL,
    cantidad                NUMERIC(18,6)   NOT NULL CHECK (cantidad > 0),
    pickeado                BOOLEAN         NOT NULL DEFAULT false,
    descargado              BOOLEAN         NOT NULL DEFAULT false,
    recibido                BOOLEAN         NOT NULL DEFAULT false,
    version                 BIGINT          NOT NULL DEFAULT 0,
    creado_en               TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en           TIMESTAMPTZ     NOT NULL DEFAULT now()
);

COMMENT ON TABLE inv_transferencia_contenedores IS 'Líneas de transferencia por contenedor/barcode (modalidad POR_CONTENEDOR)';
```

---

#### inv_ajustes (cabecera de ajuste)

```sql
CREATE TABLE inv_ajustes (
    id                      BIGSERIAL       PRIMARY KEY,
    empresa_id              BIGINT          NOT NULL REFERENCES inv_empresas(id),
    numero_ajuste           VARCHAR(30)     NOT NULL,
    bodega_id               BIGINT          NOT NULL REFERENCES inv_bodegas(id),
    tipo_ajuste_id          BIGINT          NOT NULL REFERENCES inv_tipos_ajuste(id),
    fecha_ajuste            TIMESTAMPTZ     NOT NULL DEFAULT now(),
    estado                  VARCHAR(20)     NOT NULL DEFAULT 'BORRADOR'
                            CHECK (estado IN ('BORRADOR','CONFIRMADO','CANCELADO')),
    comentarios             TEXT,
    -- Integración contable
    partida_contable_id     BIGINT,         -- FK externa a partida contable
    -- Integración documental
    nota_remision_id        BIGINT,         -- FK externa a nota de remisión
    version                 BIGINT          NOT NULL DEFAULT 0,
    creado_por              BIGINT,
    modificado_por          BIGINT,
    creado_en               TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en           TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_numero_ajuste UNIQUE (empresa_id, numero_ajuste)
);

COMMENT ON TABLE inv_ajustes IS 'Cabecera de ajuste de inventario con integración contable y documental';
```

---

#### inv_ajuste_lineas

```sql
CREATE TABLE inv_ajuste_lineas (
    id                      BIGSERIAL       PRIMARY KEY,
    ajuste_id               BIGINT          NOT NULL REFERENCES inv_ajustes(id),
    contenedor_id           BIGINT          NOT NULL REFERENCES inv_contenedores(id),
    codigo_barras           VARCHAR(100)    NOT NULL,
    producto_id             BIGINT          NOT NULL,
    cantidad                NUMERIC(18,6)   NOT NULL,   -- positivo o negativo
    precio_unitario         NUMERIC(18,6)   NOT NULL DEFAULT 0,
    operacion_id            BIGINT          REFERENCES inv_operaciones(id),
    comentarios             TEXT,
    creado_en               TIMESTAMPTZ     NOT NULL DEFAULT now()
);

COMMENT ON TABLE inv_ajuste_lineas IS 'Detalle de ajuste por contenedor';
```

---

#### inv_ordenes_picking (cabecera de picking)

```sql
CREATE TABLE inv_ordenes_picking (
    id                          BIGSERIAL       PRIMARY KEY,
    empresa_id                  BIGINT          NOT NULL REFERENCES inv_empresas(id),
    numero_picking              VARCHAR(30)     NOT NULL,
    bodega_id                   BIGINT          NOT NULL REFERENCES inv_bodegas(id),
    tipo_picking                VARCHAR(30)     NOT NULL DEFAULT 'REQUISICION'
                                CHECK (tipo_picking IN ('REQUISICION','ORDEN_VENTA','PRODUCCION','GENERAL')),
    tipo_referencia_origen      VARCHAR(50),
    referencia_origen_id        BIGINT,
    estado                      VARCHAR(20)     NOT NULL DEFAULT 'PENDIENTE'
                                CHECK (estado IN ('PENDIENTE','EN_PROCESO','COMPLETADO','CANCELADO')),
    fecha_picking               TIMESTAMPTZ     NOT NULL DEFAULT now(),
    fecha_completado            TIMESTAMPTZ,
    comentarios                 TEXT,
    version                     BIGINT          NOT NULL DEFAULT 0,
    creado_por                  BIGINT,
    modificado_por              BIGINT,
    creado_en                   TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en               TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_numero_picking UNIQUE (empresa_id, numero_picking)
);

COMMENT ON TABLE inv_ordenes_picking IS 'Órdenes de picking vinculadas a requisiciones, ventas o producción';
```

---

#### inv_picking_lineas

```sql
CREATE TABLE inv_picking_lineas (
    id                      BIGSERIAL       PRIMARY KEY,
    orden_picking_id        BIGINT          NOT NULL REFERENCES inv_ordenes_picking(id),
    producto_id             BIGINT          NOT NULL,
    unidad_id               BIGINT          NOT NULL REFERENCES inv_unidades(id),
    cantidad_solicitada     NUMERIC(18,6)   NOT NULL CHECK (cantidad_solicitada > 0),
    cantidad_pickeada       NUMERIC(18,6)   NOT NULL DEFAULT 0,
    contenedor_id           BIGINT          REFERENCES inv_contenedores(id),
    codigo_barras           VARCHAR(100),
    ubicacion_id            BIGINT          REFERENCES inv_ubicaciones(id),
    operacion_id            BIGINT          REFERENCES inv_operaciones(id),
    reserva_id              BIGINT          REFERENCES inv_reservas(id),
    estado                  VARCHAR(20)     NOT NULL DEFAULT 'PENDIENTE'
                            CHECK (estado IN ('PENDIENTE','PARCIAL','COMPLETADO','CANCELADO')),
    version                 BIGINT          NOT NULL DEFAULT 0,
    creado_en               TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en           TIMESTAMPTZ     NOT NULL DEFAULT now()
);

COMMENT ON TABLE inv_picking_lineas IS 'Líneas de picking: qué producto, de qué contenedor, cuánto';
```

---

#### inv_conteos_fisicos (conteo físico)

```sql
CREATE TABLE inv_conteos_fisicos (
    id                      BIGSERIAL       PRIMARY KEY,
    empresa_id              BIGINT          NOT NULL REFERENCES inv_empresas(id),
    numero_conteo           VARCHAR(30)     NOT NULL,
    bodega_id               BIGINT          NOT NULL REFERENCES inv_bodegas(id),
    fecha_conteo            DATE            NOT NULL,
    estado                  VARCHAR(20)     NOT NULL DEFAULT 'BORRADOR'
                            CHECK (estado IN ('BORRADOR','EN_PROCESO','REVISADO','APLICADO','CANCELADO')),
    comentarios             TEXT,
    version                 BIGINT          NOT NULL DEFAULT 0,
    creado_por              BIGINT,
    modificado_por          BIGINT,
    creado_en               TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en           TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_numero_conteo UNIQUE (empresa_id, numero_conteo)
);

COMMENT ON TABLE inv_conteos_fisicos IS 'Cabecera de conteo físico / inventario cíclico';
```

---

#### inv_conteo_lineas

```sql
CREATE TABLE inv_conteo_lineas (
    id                      BIGSERIAL       PRIMARY KEY,
    conteo_fisico_id        BIGINT          NOT NULL REFERENCES inv_conteos_fisicos(id),
    contenedor_id           BIGINT          REFERENCES inv_contenedores(id),
    codigo_barras           VARCHAR(100),
    producto_id             BIGINT          NOT NULL,
    ubicacion_id            BIGINT          REFERENCES inv_ubicaciones(id),
    unidad_id               BIGINT          NOT NULL REFERENCES inv_unidades(id),
    cantidad_sistema        NUMERIC(18,6)   NOT NULL DEFAULT 0,
    cantidad_contada        NUMERIC(18,6),
    diferencia              NUMERIC(18,6)   GENERATED ALWAYS AS (cantidad_contada - cantidad_sistema) STORED,
    ajuste_aplicado         BOOLEAN         NOT NULL DEFAULT false,
    operacion_id            BIGINT          REFERENCES inv_operaciones(id),
    comentarios             TEXT,
    version                 BIGINT          NOT NULL DEFAULT 0,
    creado_en               TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en           TIMESTAMPTZ     NOT NULL DEFAULT now()
);

COMMENT ON TABLE inv_conteo_lineas IS 'Líneas de conteo: sistema vs real, diferencia calculada automáticamente';
COMMENT ON COLUMN inv_conteo_lineas.diferencia IS 'Columna generada: cantidad_contada - cantidad_sistema';
```

---

### 3.4. EXTENSIONES

#### inv_maximos_minimos

```sql
CREATE TABLE inv_maximos_minimos (
    id                      BIGSERIAL       PRIMARY KEY,
    empresa_id              BIGINT          NOT NULL REFERENCES inv_empresas(id),
    producto_id             BIGINT          NOT NULL,
    unidad_id               BIGINT          NOT NULL REFERENCES inv_unidades(id),
    bodega_id               BIGINT          REFERENCES inv_bodegas(id),   -- NULL = global
    stock_minimo            NUMERIC(18,6)   NOT NULL DEFAULT 0,
    stock_maximo            NUMERIC(18,6)   NOT NULL DEFAULT 0,
    punto_reorden           NUMERIC(18,6)   NOT NULL DEFAULT 0,
    stock_actual            NUMERIC(18,6)   NOT NULL DEFAULT 0,             -- se recalcula por servicio
    estado_stock            VARCHAR(20)     NOT NULL DEFAULT 'EN_RANGO'
                            CHECK (estado_stock IN ('BAJO','EN_RANGO','EXCESO')),
    ultimo_calculo_en       TIMESTAMPTZ,
    activo                  BOOLEAN         NOT NULL DEFAULT true,
    version                 BIGINT          NOT NULL DEFAULT 0,
    creado_en               TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en           TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_maxmin_producto UNIQUE (empresa_id, producto_id, unidad_id, bodega_id)
);

CREATE INDEX idx_maxmin_estado ON inv_maximos_minimos(estado_stock) WHERE activo = true;
CREATE INDEX idx_maxmin_producto ON inv_maximos_minimos(producto_id);

COMMENT ON TABLE inv_maximos_minimos IS 'Configuración de máximos y mínimos por producto, unidad y bodega';
COMMENT ON COLUMN inv_maximos_minimos.stock_actual IS 'Recalculado por el servicio, no se edita directo';
```

---

#### inv_config_merma

```sql
CREATE TABLE inv_config_merma (
    id                          BIGSERIAL       PRIMARY KEY,
    empresa_id                  BIGINT          NOT NULL REFERENCES inv_empresas(id),
    habilitado                  BOOLEAN         NOT NULL DEFAULT false,
    porcentaje_defecto          NUMERIC(5,2)    NOT NULL DEFAULT 0,
    aplicar_auto_en_recepcion   BOOLEAN         NOT NULL DEFAULT false,
    cuenta_contable_id          BIGINT,         -- FK externa a cuenta contable
    version                     BIGINT          NOT NULL DEFAULT 0,
    creado_en                   TIMESTAMPTZ     NOT NULL DEFAULT now(),
    modificado_en               TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_config_merma_empresa UNIQUE (empresa_id)
);

COMMENT ON TABLE inv_config_merma IS 'Configuración global de merma por empresa';
```

---

#### inv_registros_merma

```sql
CREATE TABLE inv_registros_merma (
    id                      BIGSERIAL       PRIMARY KEY,
    empresa_id              BIGINT          NOT NULL REFERENCES inv_empresas(id),
    contenedor_id           BIGINT          REFERENCES inv_contenedores(id),
    codigo_barras           VARCHAR(100),
    producto_id             BIGINT          NOT NULL,
    bodega_id               BIGINT          NOT NULL REFERENCES inv_bodegas(id),
    cantidad                NUMERIC(18,6)   NOT NULL CHECK (cantidad > 0),
    costo_unitario          NUMERIC(18,6)   NOT NULL DEFAULT 0,
    costo_total             NUMERIC(18,6)   NOT NULL DEFAULT 0,
    tipo_merma              VARCHAR(20)     NOT NULL DEFAULT 'MANUAL'
                            CHECK (tipo_merma IN ('MANUAL','AUTOMATICA','POR_PRODUCTO')),
    operacion_id            BIGINT          REFERENCES inv_operaciones(id),
    partida_contable_id     BIGINT,
    comentarios             TEXT,
    creado_por              BIGINT,
    creado_en               TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX idx_merma_producto ON inv_registros_merma(producto_id);
CREATE INDEX idx_merma_fecha ON inv_registros_merma(creado_en);

COMMENT ON TABLE inv_registros_merma IS 'Registro histórico de merma con trazabilidad a operación y contabilidad';
```

---

#### inv_fotos_costo

```sql
CREATE TABLE inv_fotos_costo (
    id                      BIGSERIAL       PRIMARY KEY,
    empresa_id              BIGINT          NOT NULL REFERENCES inv_empresas(id),
    producto_id             BIGINT          NOT NULL,
    bodega_id               BIGINT          REFERENCES inv_bodegas(id),
    fecha_foto              DATE            NOT NULL,
    cantidad_existencia     NUMERIC(18,6)   NOT NULL DEFAULT 0,
    costo_unitario_prom     NUMERIC(18,6)   NOT NULL DEFAULT 0,
    costo_total             NUMERIC(18,6)   NOT NULL DEFAULT 0,
    metodo_costeo           VARCHAR(20)     NOT NULL DEFAULT 'PROMEDIO_PONDERADO'
                            CHECK (metodo_costeo IN ('PROMEDIO_PONDERADO','FIJO','PEPS','UEPS')),
    creado_en               TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_foto_costo UNIQUE (empresa_id, producto_id, bodega_id, fecha_foto)
);

CREATE INDEX idx_foto_costo_fecha ON inv_fotos_costo(fecha_foto DESC);

COMMENT ON TABLE inv_fotos_costo IS 'Foto periódica de valorización de inventario para reportes y auditoría';
COMMENT ON COLUMN inv_fotos_costo.metodo_costeo IS 'PROMEDIO_PONDERADO, FIJO, PEPS (primero en entrar), UEPS (último en entrar)';
```

---

#### inv_secuencias_barcode

```sql
CREATE TABLE inv_secuencias_barcode (
    id                      BIGSERIAL       PRIMARY KEY,
    empresa_id              BIGINT          NOT NULL REFERENCES inv_empresas(id),
    prefijo                 VARCHAR(20)     NOT NULL DEFAULT 'INV',
    ultimo_numero           BIGINT          NOT NULL DEFAULT 0,
    longitud_relleno        INT             NOT NULL DEFAULT 8,

    CONSTRAINT uq_secuencia_barcode_empresa UNIQUE (empresa_id, prefijo)
);

COMMENT ON TABLE inv_secuencias_barcode IS 'Secuencias para generación automática de códigos de barras';
COMMENT ON COLUMN inv_secuencias_barcode.longitud_relleno IS 'Largo del número con ceros: 8 = INV00000001';
```

---

#### inv_auditoria

```sql
CREATE TABLE inv_auditoria (
    id                      BIGSERIAL       PRIMARY KEY,
    empresa_id              BIGINT          NOT NULL,
    nombre_tabla            VARCHAR(100)    NOT NULL,
    registro_id             BIGINT          NOT NULL,
    accion                  VARCHAR(15)     NOT NULL CHECK (accion IN ('INSERTAR','ACTUALIZAR','ELIMINAR')),
    valores_anteriores      JSONB,
    valores_nuevos          JSONB,
    campos_modificados      TEXT[],
    usuario_id              BIGINT,
    direccion_ip            VARCHAR(45),
    creado_en               TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX idx_auditoria_tabla_registro ON inv_auditoria(nombre_tabla, registro_id);
CREATE INDEX idx_auditoria_fecha ON inv_auditoria(creado_en DESC);
CREATE INDEX idx_auditoria_usuario ON inv_auditoria(usuario_id);

COMMENT ON TABLE inv_auditoria IS 'Log de auditoría para cambios críticos en catálogos y contenedores';
```

---

## 4. Vistas útiles para consultas frecuentes

#### v_stock_contenedor -- stock actual por contenedor

```sql
CREATE OR REPLACE VIEW v_stock_contenedor AS
SELECT
    c.id                    AS contenedor_id,
    c.empresa_id,
    c.codigo_barras,
    c.producto_id,
    c.proveedor_id,
    c.unidad_id,
    c.bodega_id,
    c.ubicacion_id,
    c.precio_unitario,
    c.numero_lote,
    c.fecha_vencimiento,
    c.estado_id,
    COALESCE(SUM(o.cantidad) FILTER (WHERE o.activo), 0) AS cantidad_stock,
    COALESCE(SUM(r.cantidad_reservada - r.cantidad_despachada)
             FILTER (WHERE r.estado IN ('PENDIENTE','PARCIAL')), 0) AS cantidad_reservada,
    COALESCE(SUM(o.cantidad) FILTER (WHERE o.activo), 0)
        - COALESCE(SUM(r.cantidad_reservada - r.cantidad_despachada)
                   FILTER (WHERE r.estado IN ('PENDIENTE','PARCIAL')), 0) AS cantidad_disponible
FROM inv_contenedores c
LEFT JOIN inv_operaciones o ON o.contenedor_id = c.id AND o.activo = true
LEFT JOIN inv_reservas r ON r.contenedor_id = c.id AND r.estado IN ('PENDIENTE','PARCIAL')
WHERE c.activo = true
GROUP BY c.id, c.empresa_id, c.codigo_barras, c.producto_id, c.proveedor_id,
         c.unidad_id, c.bodega_id, c.ubicacion_id, c.precio_unitario,
         c.numero_lote, c.fecha_vencimiento, c.estado_id;

COMMENT ON VIEW v_stock_contenedor IS 'Stock actual, reservado y disponible por contenedor';
```

---

#### v_stock_producto_bodega -- stock por producto y bodega

```sql
CREATE OR REPLACE VIEW v_stock_producto_bodega AS
SELECT
    o.empresa_id,
    o.producto_id,
    o.bodega_id,
    o.unidad_id,
    SUM(o.cantidad) AS cantidad_stock
FROM inv_operaciones o
WHERE o.activo = true
GROUP BY o.empresa_id, o.producto_id, o.bodega_id, o.unidad_id
HAVING SUM(o.cantidad) <> 0;

COMMENT ON VIEW v_stock_producto_bodega IS 'Stock agregado por producto + bodega + unidad';
```

---

#### v_contenedores_por_vencer -- contenedores próximos a vencer

```sql
CREATE OR REPLACE VIEW v_contenedores_por_vencer AS
SELECT
    cs.*,
    c.fecha_vencimiento AS fecha_vence,
    (c.fecha_vencimiento - CURRENT_DATE) AS dias_para_vencer
FROM v_stock_contenedor cs
JOIN inv_contenedores c ON c.id = cs.contenedor_id
WHERE c.fecha_vencimiento IS NOT NULL
  AND cs.cantidad_stock > 0
  AND c.fecha_vencimiento <= CURRENT_DATE + INTERVAL '90 days'
ORDER BY c.fecha_vencimiento ASC;

COMMENT ON VIEW v_contenedores_por_vencer IS 'Contenedores con vencimiento en los próximos 90 días';
```

---

## 5. Funciones auxiliares en PostgreSQL

#### Función para calcular stock de un contenedor

```sql
CREATE OR REPLACE FUNCTION fn_stock_contenedor(p_contenedor_id BIGINT)
RETURNS NUMERIC AS $$
    SELECT COALESCE(SUM(cantidad), 0)
    FROM inv_operaciones
    WHERE contenedor_id = p_contenedor_id AND activo = true;
$$ LANGUAGE SQL STABLE;
```

#### Función para siguiente código de barras

```sql
CREATE OR REPLACE FUNCTION fn_siguiente_codigo_barras(p_empresa_id BIGINT, p_prefijo VARCHAR DEFAULT 'INV')
RETURNS VARCHAR AS $$
DECLARE
    v_siguiente BIGINT;
    v_relleno INT;
BEGIN
    UPDATE inv_secuencias_barcode
    SET ultimo_numero = ultimo_numero + 1
    WHERE empresa_id = p_empresa_id AND prefijo = p_prefijo
    RETURNING ultimo_numero, longitud_relleno INTO v_siguiente, v_relleno;

    IF NOT FOUND THEN
        INSERT INTO inv_secuencias_barcode (empresa_id, prefijo, ultimo_numero, longitud_relleno)
        VALUES (p_empresa_id, p_prefijo, 1, 8)
        RETURNING ultimo_numero, longitud_relleno INTO v_siguiente, v_relleno;
    END IF;

    RETURN p_prefijo || LPAD(v_siguiente::TEXT, v_relleno, '0');
END;
$$ LANGUAGE plpgsql;
```

#### Función para costo promedio ponderado

```sql
CREATE OR REPLACE FUNCTION fn_costo_promedio_ponderado(
    p_empresa_id BIGINT,
    p_producto_id BIGINT,
    p_bodega_id BIGINT DEFAULT NULL
)
RETURNS NUMERIC AS $$
    SELECT CASE
        WHEN SUM(cantidad) FILTER (WHERE cantidad > 0) = 0 THEN 0
        ELSE SUM(cantidad * precio_unitario) FILTER (WHERE cantidad > 0)
             / SUM(cantidad) FILTER (WHERE cantidad > 0)
    END
    FROM inv_operaciones
    WHERE empresa_id = p_empresa_id
      AND producto_id = p_producto_id
      AND activo = true
      AND (p_bodega_id IS NULL OR bodega_id = p_bodega_id);
$$ LANGUAGE SQL STABLE;
```

---

## 6. Resumen de tablas

| # | Tabla | Grupo | Propósito |
|---|-------|-------|-----------|
| 1 | inv_empresas | Catálogo | Multi-empresa / tenant |
| 2 | inv_bodegas | Catálogo | Bodegas |
| 3 | inv_ubicaciones | Catálogo | Ubicaciones por bodega |
| 4 | inv_unidades | Catálogo | Unidades de medida |
| 5 | inv_conversiones | Catálogo | Reglas de conversión |
| 6 | inv_tipos_operacion | Catálogo | Tipos de operación del kardex |
| 7 | inv_tipos_ajuste | Catálogo | Razones de ajuste |
| 8 | inv_estados_contenedor | Catálogo | Estados del contenedor |
| 9 | inv_estados_transferencia | Catálogo | Estados de transferencia |
| 10 | inv_lotes | Núcleo | Maestro de lotes |
| 11 | inv_contenedores | Núcleo | Contenedor trazable por codigo_barras |
| 12 | inv_operaciones | Núcleo | Kardex de movimientos |
| 13 | inv_reservas | Núcleo | Reservas de stock |
| 14 | inv_recepciones | Flujo | Cabecera de recepción |
| 15 | inv_recepcion_lineas | Flujo | Detalle de recepción |
| 16 | inv_transferencias | Flujo | Cabecera de transferencia |
| 17 | inv_transferencia_lineas | Flujo | Detalle transferencia por producto |
| 18 | inv_transferencia_contenedores | Flujo | Detalle transferencia por contenedor |
| 19 | inv_ajustes | Flujo | Cabecera de ajuste |
| 20 | inv_ajuste_lineas | Flujo | Detalle de ajuste |
| 21 | inv_ordenes_picking | Flujo | Cabecera de picking |
| 22 | inv_picking_lineas | Flujo | Detalle de picking |
| 23 | inv_conteos_fisicos | Flujo | Conteo físico |
| 24 | inv_conteo_lineas | Flujo | Detalle de conteo |
| 25 | inv_maximos_minimos | Extensión | Máximos y mínimos |
| 26 | inv_config_merma | Extensión | Configuración de merma |
| 27 | inv_registros_merma | Extensión | Registros de merma |
| 28 | inv_fotos_costo | Extensión | Fotos de valorización |
| 29 | inv_secuencias_barcode | Extensión | Secuencias de barcode |
| 30 | inv_auditoria | Extensión | Auditoría de cambios |

**Total: 30 tablas + 3 vistas + 3 funciones**

---

## 7. Notas para la implementación en Java/Spring Boot

### Mapeo sugerido de entidades JPA

Cada tabla mapeará a una `@Entity` en Java. Las relaciones se manejan con `@ManyToOne` / `@OneToMany` lazy por defecto. Las consultas de stock NUNCA deben hacerse cargando todas las operaciones en memoria -- siempre usar queries nativos o JPQL con `SUM`.

### Enums en Java que mapean a catálogos

```
TipoOperacionCodigo    → inv_tipos_operacion.codigo
TipoAjusteCodigo       → inv_tipos_ajuste.codigo
EstadoContenedorCodigo → inv_estados_contenedor.codigo
EstadoTransferenciaCodigo → inv_estados_transferencia.codigo
TipoTransferencia      → POR_CONTENEDOR, POR_PRODUCTO
TipoRecepcion          → MANUAL, ORDEN_COMPRA, TRANSFERENCIA, PRODUCCION, DEVOLUCION
TipoPicking            → REQUISICION, ORDEN_VENTA, PRODUCCION, GENERAL
EstadoLote             → ACTIVO, CUARENTENA, VENCIDO, BLOQUEADO, CONSUMIDO
EstadoStock            → BAJO, EN_RANGO, EXCESO
MetodoCosteo           → PROMEDIO_PONDERADO, FIJO, PEPS, UEPS
```

### Índices clave para rendimiento

Los índices más importantes del modelo son los de `inv_operaciones`:
- `idx_operacion_stock_por_codigo_barras` -- para stock por contenedor
- `idx_operacion_stock_por_producto_bodega` -- para stock por producto+bodega
- `idx_operacion_kardex` -- para kardex con paginación (empresa_id, fecha_operacion DESC, id DESC)
- `idx_operacion_fecha` -- para kardex con rango de fechas

Y el índice FEFO de `inv_contenedores`:
- `idx_contenedor_fefo` -- para picking ordenado por vencimiento y antigüedad

Si el volumen crece mucho, considerar particionamiento por `fecha_operacion` en `inv_operaciones`.
