# CLAUDE.md — Contexto del Proyecto para Claude Code

> Este archivo es leído automáticamente por Claude Code (CLI de Anthropic) al trabajar en este repositorio.
> Contiene el contexto mínimo necesario para que Claude entienda el proyecto y genere código correcto.

---

## Qué es este proyecto

Módulo de inventario del ERP Bilans. Sistema de trazabilidad basado en **contenedores** (código de barras)
y **kardex de operaciones** (movimientos). El stock NUNCA se persiste como columna; siempre se calcula
como `SUM(cantidad)` de las operaciones activas.

## Stack

- **Java 21 (LTS)** + **Spring Boot 4.0.x** (Spring Framework 7.x) + **Maven**
- **PostgreSQL 18** con Flyway 11.x para migraciones
- **Angular 21** como frontend (consume la API REST)
- **MapStruct 1.6.3** para mapeo Entity ↔ DTO
- **Lombok 1.18.44** para boilerplate
- **SpringDoc OpenAPI 3.x** (Swagger) para documentación de API — v3.x es la compatible con Spring Boot 4
- **Testcontainers** para tests de integración

## Comandos útiles

```bash
# Compilar
mvn clean compile

# Ejecutar tests
mvn test

# Ejecutar la aplicación
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Generar reporte de cobertura
mvn verify -Pcoverage

# Solo ejecutar tests unitarios
mvn test -Dgroups="unit"

# Solo ejecutar tests de integración
mvn test -Dgroups="integration"

# Verificar que Flyway aplica correctamente
mvn flyway:validate

# Generar Javadoc
mvn javadoc:javadoc
```

## Estructura de paquetes

```
com.bilans.inventory/
├── config/              → Configuración Spring (OpenAPI, JPA Audit, Flyway)
├── domain/
│   ├── entity/          → Entidades JPA (subcarpetas: catalogo, nucleo, flujo, extension)
│   ├── enums/           → Enums del dominio (valores en español, coinciden con BD)
│   └── base/            → BaseEntity, AuditableEntity
├── repository/          → Spring Data JPA + queries nativos
│   └── projection/      → Interfaces de proyección
├── service/             → Interfaces de servicios
├── service/impl/        → Implementaciones (@Service)
├── dto/request/         → DTOs de entrada (Java records + @Valid + @Schema)
├── dto/response/        → DTOs de salida (Java records + @Schema)
├── controller/          → REST controllers (@RestController + Swagger)
├── mapper/              → MapStruct mappers
├── exception/           → Excepciones de dominio + GlobalExceptionHandler
├── event/               → Eventos de dominio (Spring ApplicationEvent)
├── listener/            → Listeners de eventos
├── scheduler/           → Tareas programadas (@Scheduled)
├── integration/         → Integraciones con otros módulos
└── util/                → Constantes y utilidades
```

## Reglas fundamentales que Claude DEBE seguir

### Base de datos en español

- Tablas: `inv_contenedores`, `inv_operaciones`, `inv_bodegas`, `inv_ubicaciones`
- Columnas: `codigo_barras`, `precio_unitario`, `fecha_vencimiento`, `cantidad`
- Valores de catálogo: `RECEPCION`, `AJUSTE_POSITIVO`, `SALIDA_TRANSFERENCIA`, `DISPONIBLE`
- Funciones: `fn_stock_contenedor()`, `fn_siguiente_barcode()`, `fn_costo_promedio_ponderado()`
- El mapeo Java usa `@Table(name = "inv_contenedores")` y `@Column(name = "codigo_barras")`

### Stock calculado, nunca persistido

```sql
-- SIEMPRE así:
SELECT COALESCE(SUM(cantidad), 0) FROM inv_operaciones WHERE contenedor_id = ? AND activo = true;
```

Nunca crear columnas tipo `stock_actual` en tablas de productos o contenedores (excepto en `inv_maximos_minimos`
donde es un campo recalculado por servicio, no editado directamente).

### Toda operación pasa por OperacionService

```java
// ✅ SIEMPRE
operacionService.crearOperacion(contenedor, TipoOperacionCodigo.RECEPCION, cantidad, comentario);

// ❌ NUNCA
operacionRepository.save(nuevaOperacion);
```

### Patrón Service Interface + Impl

- `RecepcionService` (interface en `service/flujo/`)
- `RecepcionServiceImpl` (implementación en `service/impl/flujo/`)
- El controller solo conoce la interface.

### DTOs obligatorios

- Controllers NUNCA reciben ni retornan entidades JPA.
- Request DTOs: Java records con `@Valid`, `@NotNull`, `@Schema`.
- Response DTOs: Java records con `@Schema`.
- Conversión vía MapStruct.

### Swagger obligatorio

Todo controller DEBE tener:
- `@Tag` a nivel de clase
- `@Operation` en cada método
- `@ApiResponses` con códigos posibles
- `@Parameter` en path variables y query params

Todo DTO DEBE tener `@Schema` en cada campo.

### Respuesta estándar

```java
ResponseEntity<ApiResponse<T>>          // para un objeto
ResponseEntity<ApiResponse<PaginaResponse<T>>>  // para listados paginados
```

### URLs REST en español y kebab-case

```
/api/v1/recepciones
/api/v1/transferencias
/api/v1/conteos-fisicos
/api/v1/maximos-minimos
/api/v1/inventario/stock/producto-bodega
```

### Transaccionalidad

- Servicios de escritura: `@Transactional`
- Servicios de lectura: `@Transactional(readOnly = true)`
- Un flujo completo = una transacción. Si falla algo, todo se revierte.

## Lo que NO debes hacer

1. No persistir stock como columna.
2. No exponer entidades JPA en controllers.
3. No poner lógica de negocio en controllers.
4. No acceder a repositorios desde controllers.
5. No crear operaciones sin pasar por `OperacionService`.
6. No usar números mágicos — siempre usar enums o catálogos.
7. No hardcodear IDs (como `ubicacion_id = 1`).
8. No modificar migraciones Flyway ya existentes — crear nuevas.
9. No dejar endpoints sin anotaciones Swagger.
10. No usar `open-in-view = true`.
11. No cargar listas completas de operaciones a memoria para calcular stock.

## Enums principales (valores coinciden con BD)

```java
TipoOperacionCodigo:
  RECEPCION, PICKING, AJUSTE_POSITIVO, AJUSTE_NEGATIVO, AJUSTE_INFORMATIVO,
  SALIDA_TRANSFERENCIA, ENTRADA_TRANSFERENCIA, SALIDA_MOVIMIENTO, ENTRADA_MOVIMIENTO,
  SALIDA_CONVERSION, ENTRADA_CONVERSION, INGRESO_PRODUCCION, MERMA, AJUSTE_VENTA,
  CONTEO_POSITIVO, CONTEO_NEGATIVO

EstadoContenedorCodigo:
  DISPONIBLE, RESERVADO, EN_TRANSITO, EN_STANDBY, CUARENTENA, BLOQUEADO, AGOTADO

EstadoTransferenciaCodigo:
  BORRADOR, CONFIRMADO, DESPACHADO, EN_TRANSITO, RECIBIDO_PARCIAL, RECIBIDO_COMPLETO,
  CANCELADO, CIERRE_FORZADO

TipoReferencia:
  RECEPCION, TRANSFERENCIA, PICKING, AJUSTE, ORDEN_COMPRA, VENTA,
  ORDEN_PRODUCCION, CONTEO_FISICO
```

## Tablas principales (30 en total)

**Catálogos (9):** `inv_empresas`, `inv_bodegas`, `inv_ubicaciones`, `inv_unidades`, `inv_conversiones`,
`inv_tipos_operacion`, `inv_tipos_ajuste`, `inv_estados_contenedor`, `inv_estados_transferencia`

**Núcleo (4):** `inv_lotes`, `inv_contenedores`, `inv_operaciones`, `inv_reservas`

**Flujos (11):** `inv_recepciones`, `inv_recepcion_lineas`, `inv_transferencias`, `inv_transferencia_lineas`,
`inv_transferencia_contenedores`, `inv_ajustes`, `inv_ajuste_lineas`, `inv_ordenes_picking`,
`inv_picking_lineas`, `inv_conteos_fisicos`, `inv_conteo_lineas`

**Extensiones (6):** `inv_maximos_minimos`, `inv_config_merma`, `inv_registros_merma`,
`inv_fotos_costo`, `inv_secuencias_barcode`, `inv_auditoria`

## Servicios principales

| Servicio | Responsabilidad |
|----------|----------------|
| `OperacionService` | Crea TODA operación en el kardex. Punto único obligatorio. |
| `StockQueryService` | Consultas de stock. Solo lectura. |
| `BarcodeService` | Genera y valida códigos de barras. |
| `RecepcionService` | Recepción: manual, compra, traslado, producción, devolución. |
| `AjusteInventarioService` | Ajustes +/- de cantidad y ajuste de precio. |
| `MoverContenedorService` | Movimientos entre ubicaciones y standby. |
| `ConvertirUnidadService` | Conversión de unidades de un contenedor. |
| `TransferenciaPorContenedorService` | Traslado legacy por barcode. |
| `TransferenciaPorProductoService` | Traslado por producto con resolución FIFO. |
| `PickingService` | Picking por requisición, venta o general. |
| `ConteoFisicoService` | Conteo físico y generación de ajustes. |
| `MermaService` | Merma manual/automática. |
| `MaximosMinimosService` | Recalcula stock vs umbrales. |
| `ValorizacionCostoService` | Costo promedio y snapshots. |

## Consultas de stock — queries clave

```sql
-- Stock de un contenedor
SELECT COALESCE(SUM(cantidad), 0) FROM inv_operaciones
WHERE contenedor_id = :id AND activo = true;

-- Stock por producto y bodega
SELECT COALESCE(SUM(cantidad), 0) FROM inv_operaciones
WHERE producto_id = :pid AND bodega_id = :bid AND empresa_id = :eid AND activo = true;

-- Contenedores disponibles FEFO (para picking/transferencia)
SELECT c.id, c.codigo_barras, SUM(o.cantidad) as stock
FROM inv_contenedores c
JOIN inv_estados_contenedor ec ON ec.id = c.estado_id
LEFT JOIN inv_operaciones o ON o.contenedor_id = c.id AND o.activo = true
WHERE c.producto_id = :pid AND c.bodega_id = :bid AND ec.permite_picking = true AND c.activo = true
GROUP BY c.id HAVING SUM(o.cantidad) > 0
ORDER BY c.fecha_vencimiento ASC NULLS LAST, c.creado_en ASC;
```

## Archivos de referencia

- `AGENTS.md` → guía completa de arquitectura y estándares (este archivo es el resumen ejecutivo).
- `inventario_ddl_postgresql_es.sql` → DDL completo de la BD en español.
- `inventario_arquitectura_java.md` → arquitectura detallada con código de ejemplo.
- `inventario_mapeo_bd_java.md` → mapeo tabla-por-tabla entre BD y Java.
