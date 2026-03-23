# CLAUDE.md — Bootstrapping para Claude Code

> Este archivo se carga automáticamente. Contiene solo lo mínimo para arrancar.
> Para reglas completas → `.github/AGENTS.md`
> Para código de referencia → `docs/inventario_arquitectura_java.md`

---

## Qué es esto

Módulo de inventario del ERP **Exodia**. Trazabilidad basada en **contenedores** (código de barras)
y **kardex de operaciones**. El stock NUNCA se persiste; siempre se calcula como
`SUM(cantidad) FROM inv_operaciones WHERE activo = true`.

## Stack

Java 21 · Spring Boot 4.0.x · PostgreSQL 18 · Flyway 11.x · MapStruct 1.6.3 · Lombok 1.18.44 · SpringDoc OpenAPI 3.x · Angular 21 · Maven · Testcontainers

## Comandos

```bash
mvn clean compile                              # Compilar
mvn test                                       # Ejecutar tests
mvn spring-boot:run -Dspring-boot.run.profiles=dev  # Ejecutar en dev
mvn test -Dgroups="unit"                       # Solo tests unitarios
mvn test -Dgroups="integration"                # Solo tests integración
mvn verify -Pcoverage                          # Cobertura
```

## Paquete base

`com.exodia.inventario` — estructura DDD pragmático:

```
domain/          → modelo/ (por aggregate), base/, enums/, evento/, vo/, servicio/, politica/
aplicacion/      → comando/ (escritura), consulta/ (lectura)
repositorio/     → por aggregate, alineado con domain/modelo/
interfaz/        → rest/ (controllers), dto/{peticion,respuesta}, mapeador/
infraestructura/ → listener/, programacion/, integracion/ (ACL)
excepcion/       → excepciones de dominio + handler/
config/          → Spring configuration
util/            → constantes, helpers
```

## 5 reglas que NUNCA violar

1. **Stock = cálculo**, nunca columna persistida.
2. **Toda operación** pasa por `OperacionService.crearOperacion()`.
3. **Controllers** solo reciben/retornan DTOs, nunca entidades JPA.
4. **Deducción de stock** requiere lock pesimista (`findByIdForUpdate`).
5. **Todo endpoint** documentado con Swagger (`@Tag`, `@Operation`, `@ApiResponses`).

## Enums principales (valores = BD)

| Enum | Valores |
|------|---------|
| `TipoOperacionCodigo` | `RECEPCION`, `PICKING`, `AJUSTE_POSITIVO`, `AJUSTE_NEGATIVO`, `AJUSTE_INFORMATIVO`, `SALIDA_TRANSFERENCIA`, `ENTRADA_TRANSFERENCIA`, `SALIDA_MOVIMIENTO`, `ENTRADA_MOVIMIENTO`, `SALIDA_CONVERSION`, `ENTRADA_CONVERSION`, `INGRESO_PRODUCCION`, `MERMA`, `AJUSTE_VENTA`, `CONTEO_POSITIVO`, `CONTEO_NEGATIVO` |
| `EstadoContenedorCodigo` | `DISPONIBLE`, `RESERVADO`, `EN_TRANSITO`, `EN_STANDBY`, `CUARENTENA`, `BLOQUEADO`, `AGOTADO` |
| `EstadoTransferenciaCodigo` | `BORRADOR`, `CONFIRMADO`, `DESPACHADO`, `EN_TRANSITO`, `RECIBIDO_PARCIAL`, `RECIBIDO_COMPLETO`, `CANCELADO`, `CIERRE_FORZADO` |
| `TipoReferencia` | `RECEPCION`, `TRANSFERENCIA`, `PICKING`, `AJUSTE`, `ORDEN_COMPRA`, `VENTA`, `ORDEN_PRODUCCION`, `CONTEO_FISICO` |

## Tablas (30 total)

- **Catálogos (9):** `inv_empresas`, `inv_bodegas`, `inv_ubicaciones`, `inv_unidades`, `inv_conversiones`, `inv_tipos_operacion`, `inv_tipos_ajuste`, `inv_estados_contenedor`, `inv_estados_transferencia`
- **Núcleo (4):** `inv_lotes`, `inv_contenedores`, `inv_operaciones`, `inv_reservas`
- **Flujos (11):** `inv_recepciones`, `inv_recepcion_lineas`, `inv_transferencias`, `inv_transferencia_lineas`, `inv_transferencia_contenedores`, `inv_ajustes`, `inv_ajuste_lineas`, `inv_ordenes_picking`, `inv_picking_lineas`, `inv_conteos_fisicos`, `inv_conteo_lineas`
- **Extensiones (6):** `inv_maximos_minimos`, `inv_config_merma`, `inv_registros_merma`, `inv_fotos_costo`, `inv_secuencias_barcode`, `inv_auditoria`

## Servicios clave

| Capa | Servicio | Responsabilidad |
|------|----------|----------------|
| **Domain** | `CalculadorStock` | Lógica pura de cálculo de stock (Java puro, sin Spring). |
| **Domain** | `PoliticaFEFO` | Selección de contenedores por FEFO. |
| **Domain** | `PoliticaDeduccionStock` | Specification: valida si se puede deducir stock. |
| **Comando** | `OperacionService` | Crea TODA operación en el kardex. Punto único obligatorio. |
| **Comando** | `RecepcionService` | Recepción de inventario. |
| **Comando** | `AjusteInventarioService` | Ajustes +/- de cantidad y precio. |
| **Comando** | `TransferenciaService` | Traslado por producto (FIFO). |
| **Comando** | `PickingService` | Picking por requisición, venta o general. |
| **Comando** | `ConteoFisicoService` | Conteo físico con generación de ajustes. |
| **Comando** | `BarcodeService` | Genera/valida códigos de barras. |
| **Consulta** | `StockQueryService` | Consultas de stock (solo lectura, native queries). |
| **Consulta** | `KardexQueryService` | Consultas de kardex/historial. |

## Archivos de referencia

- `.github/AGENTS.md` → **reglas completas** de arquitectura, concurrencia, performance, testing.
- `docs/inventario_arquitectura_java.md` → código de referencia completo (entidades, repos, servicios).

