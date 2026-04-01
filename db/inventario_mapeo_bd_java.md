# Mapeo de nombres — Base de datos (español) ↔ Java (inglés)

Este documento sirve de referencia cruzada entre los nombres de tablas/columnas en PostgreSQL (español)
y las entidades/atributos Java que las mapean.

---

## Tablas ↔ Entidades JPA

| # | Tabla PostgreSQL (español) | Entity Java | Grupo |
|---|---|---|---|
| 1 | `inv_empresas` | `Company` | Catálogo |
| 2 | `inv_bodegas` | `Warehouse` | Catálogo |
| 3 | `inv_ubicaciones` | `WarehouseLocation` | Catálogo |
| 4 | `inv_unidades` | `Unit` | Catálogo |
| 5 | `inv_conversiones` | `UnitConversion` | Catálogo |
| 6 | `inv_tipos_operacion` | `OperationType` | Catálogo |
| 7 | `inv_tipos_ajuste` | `AdjustmentType` | Catálogo |
| 8 | `inv_estados_contenedor` | `ContainerStatus` | Catálogo |
| 9 | `inv_estados_transferencia` | `TransferStatus` | Catálogo |
| 10 | `inv_lotes` | `LotMaster` | Núcleo |
| 11 | `inv_contenedores` | `Container` | Núcleo |
| 12 | `inv_operaciones` | `Operation` | Núcleo |
| 13 | `inv_reservas` | `Reservation` | Núcleo |
| 14 | `inv_recepciones` | `Reception` | Flujo |
| 15 | `inv_recepcion_lineas` | `ReceptionLine` | Flujo |
| 16 | `inv_transferencias` | `Transfer` | Flujo |
| 17 | `inv_transferencia_lineas` | `TransferLine` | Flujo |
| 18 | `inv_transferencia_contenedores` | `TransferContainer` | Flujo |
| 19 | `inv_ajustes` | `Adjustment` | Flujo |
| 20 | `inv_ajuste_lineas` | `AdjustmentLine` | Flujo |
| 21 | `inv_ordenes_picking` | `PickingOrder` | Flujo |
| 22 | `inv_picking_lineas` | `PickingLine` | Flujo |
| 23 | `inv_conteos_fisicos` | `PhysicalCount` | Flujo |
| 24 | `inv_conteo_lineas` | `PhysicalCountLine` | Flujo |
| 25 | `inv_maximos_minimos` | `MinMaxConfig` | Extensión |
| 26 | `inv_config_merma` | `ShrinkageConfig` | Extensión |
| 27 | `inv_registros_merma` | `ShrinkageRecord` | Extensión |
| 28 | `inv_fotos_costo` | `CostSnapshot` | Extensión |
| 29 | `inv_secuencias_barcode` | `BarcodeSequence` | Extensión |
| 30 | `inv_auditoria` | `AuditLog` | Extensión |

---

## Columnas comunes — Mapeo de nombres

| Columna PostgreSQL | Atributo Java | Tipo Java |
|---|---|---|
| `id` | `id` | `Long` |
| `empresa_id` | `company` / `companyId` | `Company` / `Long` |
| `codigo` | `code` | `String` |
| `nombre` | `name` | `String` |
| `activo` | `isActive` / `active` | `Boolean` |
| `creado_en` | `createdAt` | `OffsetDateTime` |
| `modificado_en` | `updatedAt` | `OffsetDateTime` |
| `creado_por` | `createdBy` | `Long` |
| `modificado_por` | `updatedBy` | `Long` |
| `codigo_barras` | `barcode` | `String` |
| `producto_id` | `productId` | `Long` |
| `proveedor_id` | `supplierId` | `Long` |
| `bodega_id` | `warehouse` | `Warehouse` |
| `ubicacion_id` | `location` | `WarehouseLocation` |
| `unidad_id` | `unit` | `Unit` |
| `precio_unitario` | `unitPrice` | `BigDecimal` |
| `numero_lote` | `lotNumber` | `String` |
| `fecha_vencimiento` | `expirationDate` | `LocalDate` |
| `cantidad` | `quantity` | `BigDecimal` |
| `fecha_operacion` | `operationDate` | `OffsetDateTime` |
| `tipo_operacion_id` | `operationType` | `OperationType` |
| `tipo_ajuste_id` | `adjustmentType` | `AdjustmentType` |
| `estado_id` | `status` | `ContainerStatus` / `TransferStatus` |
| `estado` | `status` | `String` (enum) |
| `comentarios` | `comments` | `String` |
| `tipo_referencia` | `referenceType` | `ReferenceType` (enum) |
| `referencia_id` | `referenceId` | `Long` |
| `referencia_linea_id` | `referenceLineId` | `Long` |
| `contenedor_id` | `container` | `Container` |
| `cantidad_reservada` | `reservedQty` | `BigDecimal` |
| `cantidad_despachada` | `fulfilledQty` / `dispatchedQty` | `BigDecimal` |
| `cantidad_solicitada` | `requestedQty` | `BigDecimal` |
| `cantidad_recibida` | `receivedQty` | `BigDecimal` |
| `cantidad_pickeada` | `pickedQty` | `BigDecimal` |
| `cantidad_contada` | `countedQty` | `BigDecimal` |
| `cantidad_sistema` | `systemQty` | `BigDecimal` |
| `diferencia` | `difference` | `BigDecimal` (generada) |
| `numero_serie` | `serialNumber` | `String` |
| `info_garantia` | `warrantyInfo` | `String` |
| `direccion` | `address` | `String` |
| `ciudad` | `city` | `String` |
| `pais` | `country` | `String` |
| `abreviatura` | `abbreviation` | `String` |
| `factor_conversion` | `conversionFactor` | `BigDecimal` |
| `signo` | `sign` | `Short` |
| `descripcion` | `description` | `String` |
| `requiere_doc` | `requiresDoc` | `Boolean` |
| `permite_picking` | `allowsPicking` | `Boolean` |
| `permite_traslado` | `allowsTransfer` | `Boolean` |
| `orden` | `sortOrder` | `Integer` |
| `stock_minimo` | `minStock` | `BigDecimal` |
| `stock_maximo` | `maxStock` | `BigDecimal` |
| `punto_reorden` | `reorderPoint` | `BigDecimal` |
| `stock_actual` | `currentStock` | `BigDecimal` |
| `estado_stock` | `stockStatus` | `StockStatus` (enum) |
| `porcentaje_defecto` | `defaultPercentage` | `BigDecimal` |
| `costo_unitario` | `unitCost` | `BigDecimal` |
| `costo_total` | `totalCost` | `BigDecimal` |
| `metodo_costeo` | `costMethod` | `CostMethod` (enum) |
| `prefijo` | `prefix` | `String` |
| `ultimo_numero` | `lastNumber` | `Long` |
| `longitud_relleno` | `paddingLength` | `Integer` |
| `nombre_tabla` | `tableName` | `String` |
| `registro_id` | `recordId` | `Long` |
| `accion` | `action` | `String` |
| `valores_anteriores` | `oldValues` | `String` (JSONB) |
| `valores_nuevos` | `newValues` | `String` (JSONB) |
| `campos_modificados` | `changedFields` | `List<String>` |
| `direccion_ip` | `ipAddress` | `String` |
| `version` | `version` | `Long` |

---

## Valores de catálogos — Mapeo español ↔ código Java enum

### inv_tipos_operacion.codigo ↔ OperationTypeCode

| Valor BD (español) | Enum Java |
|---|---|
| `RECEPCION` | `RECEPCION` |
| `PICKING` | `PICKING` |
| `AJUSTE_POSITIVO` | `AJUSTE_POSITIVO` |
| `AJUSTE_NEGATIVO` | `AJUSTE_NEGATIVO` |
| `AJUSTE_INFORMATIVO` | `AJUSTE_INFORMATIVO` |
| `SALIDA_TRANSFERENCIA` | `SALIDA_TRANSFERENCIA` |
| `ENTRADA_TRANSFERENCIA` | `ENTRADA_TRANSFERENCIA` |
| `SALIDA_MOVIMIENTO` | `SALIDA_MOVIMIENTO` |
| `ENTRADA_MOVIMIENTO` | `ENTRADA_MOVIMIENTO` |
| `SALIDA_CONVERSION` | `SALIDA_CONVERSION` |
| `ENTRADA_CONVERSION` | `ENTRADA_CONVERSION` |
| `INGRESO_PRODUCCION` | `INGRESO_PRODUCCION` |
| `MERMA` | `MERMA` |
| `AJUSTE_VENTA` | `AJUSTE_VENTA` |
| `CONTEO_POSITIVO` | `CONTEO_POSITIVO` |
| `CONTEO_NEGATIVO` | `CONTEO_NEGATIVO` |

### inv_estados_contenedor.codigo ↔ ContainerStatusCode

| Valor BD | Enum Java |
|---|---|
| `DISPONIBLE` | `DISPONIBLE` |
| `RESERVADO` | `RESERVADO` |
| `EN_TRANSITO` | `EN_TRANSITO` |
| `EN_STANDBY` | `EN_STANDBY` |
| `CUARENTENA` | `CUARENTENA` |
| `BLOQUEADO` | `BLOQUEADO` |
| `AGOTADO` | `AGOTADO` |

### inv_estados_transferencia.codigo ↔ TransferStatusCode

| Valor BD | Enum Java |
|---|---|
| `BORRADOR` | `BORRADOR` |
| `CONFIRMADO` | `CONFIRMADO` |
| `DESPACHADO` | `DESPACHADO` |
| `EN_TRANSITO` | `EN_TRANSITO` |
| `RECIBIDO_PARCIAL` | `RECIBIDO_PARCIAL` |
| `RECIBIDO_COMPLETO` | `RECIBIDO_COMPLETO` |
| `CANCELADO` | `CANCELADO` |
| `CIERRE_FORZADO` | `CIERRE_FORZADO` |

### Otros enums

| Concepto | Valores BD | Enum Java |
|---|---|---|
| Tipo ubicación | `GENERAL, STANDBY, TEMPORAL, RECEPCION, PRODUCCION` | `TipoUbicacion` |
| Tipo conversión | `MULTIPLICAR, DIVIDIR` | `TipoOperacionConversion` |
| Tipo recepción | `MANUAL, ORDEN_COMPRA, TRANSFERENCIA, PRODUCCION, DEVOLUCION` | `TipoRecepcion` |
| Tipo transferencia | `POR_CONTENEDOR, POR_PRODUCTO` | `TipoTransferencia` |
| Tipo picking | `REQUISICION, ORDEN_VENTA, PRODUCCION, GENERAL` | `TipoPicking` |
| Estado lote | `ACTIVO, CUARENTENA, VENCIDO, BLOQUEADO, CONSUMIDO` | `EstadoLote` |
| Estado stock | `BAJO, EN_RANGO, EXCESO` | `EstadoStock` |
| Método costeo | `PROMEDIO_PONDERADO, FIJO, PEPS, UEPS` | `MetodoCosteo` |
| Tipo merma | `MANUAL, AUTOMATICA, POR_PRODUCTO` | `TipoMerma` |
| Tipo referencia | `RECEPCION, TRANSFERENCIA, PICKING, AJUSTE, ORDEN_COMPRA, VENTA, ORDEN_PRODUCCION, CONTEO_FISICO` | `TipoReferencia` |
| Acción auditoría | `INSERTAR, ACTUALIZAR, ELIMINAR` | `AccionAuditoria` |

---

## Vistas ↔ Proyecciones Java

| Vista PostgreSQL | Interface de proyección Java |
|---|---|
| `v_stock_contenedor` | `ContainerStockProjection` |
| `v_stock_producto_bodega` | `ProductWarehouseStockProjection` |
| `v_contenedores_por_vencer` | `ExpiringContainerProjection` |

---

## Funciones ↔ Métodos de repositorio

| Función PostgreSQL | Método Java sugerido |
|---|---|
| `fn_stock_contenedor(p_contenedor_id)` | `OperationRepository.getStockByContainerId(Long)` |
| `fn_siguiente_codigo_barras(p_empresa_id, p_prefijo)` | `BarcodeService.generarBarcode(Long, String)` |
| `fn_costo_promedio_ponderado(p_empresa_id, p_producto_id, p_bodega_id)` | `CostValuationService.calcularCostoPromedio(Long, Long, Long)` |

---

## Nota sobre la convención

- **Base de datos**: todo en español (tablas, columnas, valores de catálogo, comentarios, funciones).
- **Java**: las entidades pueden mantener nombres en inglés para seguir convención estándar del ecosistema Spring,
  o cambiarse a español si el equipo lo prefiere. El mapeo `@Table(name = "inv_contenedores")` y
  `@Column(name = "codigo_barras")` resuelve la diferencia.
- **Enums Java**: los códigos coinciden exactamente con los valores almacenados en la BD (en español),
  lo que simplifica el mapeo sin necesidad de conversores adicionales.
