-- =============================================
-- Datos iniciales de catálogos
-- =============================================

-- Tipos de operación (signo: 1=entrada, -1=salida, 0=informativo)
INSERT INTO inv_tipos_operacion (codigo, nombre, signo, descripcion) VALUES
    ('RECEPCION',             'Recepción de inventario',                 1,  NULL),
    ('PICKING',               'Picking / despacho',                     -1,  NULL),
    ('AJUSTE_POSITIVO',       'Ajuste positivo de cantidad',             1,  NULL),
    ('AJUSTE_NEGATIVO',       'Ajuste negativo de cantidad',            -1,  NULL),
    ('AJUSTE_INFORMATIVO',    'Ajuste informativo (sin cambio de stock)', 0, NULL),
    ('SALIDA_TRANSFERENCIA',  'Salida por transferencia',               -1,  NULL),
    ('ENTRADA_TRANSFERENCIA', 'Entrada por transferencia',               1,  NULL),
    ('SALIDA_MOVIMIENTO',     'Salida por movimiento',                  -1,  NULL),
    ('ENTRADA_MOVIMIENTO',    'Entrada por movimiento',                  1,  NULL),
    ('SALIDA_CONVERSION',     'Salida por conversión de unidad',        -1,  NULL),
    ('ENTRADA_CONVERSION',    'Entrada por conversión de unidad',        1,  NULL),
    ('INGRESO_PRODUCCION',    'Ingreso por producción',                  1,  NULL),
    ('MERMA',                 'Merma de inventario',                    -1,  NULL),
    ('AJUSTE_VENTA',          'Ajuste por venta',                       -1,  NULL),
    ('CONTEO_POSITIVO',       'Ajuste positivo por conteo físico',       1,  NULL),
    ('CONTEO_NEGATIVO',       'Ajuste negativo por conteo físico',      -1,  NULL);

-- Tipos de ajuste
INSERT INTO inv_tipos_ajuste (codigo, nombre) VALUES
    ('CANTIDAD',        'Ajuste de cantidad'),
    ('PRECIO',          'Ajuste de precio'),
    ('CANTIDAD_PRECIO', 'Ajuste de cantidad y precio');

-- Estados de contenedor (permite_picking, permite_transferencia)
INSERT INTO inv_estados_contenedor (codigo, nombre, permite_picking, permite_transferencia) VALUES
    ('DISPONIBLE',  'Disponible',     true,  true),
    ('RESERVADO',   'Reservado',      false, false),
    ('EN_TRANSITO', 'En tránsito',    false, false),
    ('EN_STANDBY',  'En standby',     false, false),
    ('CUARENTENA',  'En cuarentena',  false, false),
    ('BLOQUEADO',   'Bloqueado',      false, false),
    ('AGOTADO',     'Agotado',        false, false);

-- Estados de transferencia
INSERT INTO inv_estados_transferencia (codigo, nombre) VALUES
    ('BORRADOR',         'Borrador'),
    ('CONFIRMADO',       'Confirmado'),
    ('DESPACHADO',       'Despachado'),
    ('EN_TRANSITO',      'En tránsito'),
    ('RECIBIDO_PARCIAL', 'Recibido parcialmente'),
    ('RECIBIDO_COMPLETO','Recibido completo'),
    ('CANCELADO',        'Cancelado'),
    ('CIERRE_FORZADO',   'Cierre forzado');
