# AGENTS.md — Guía de Arquitectura y Estándares para el Módulo de Inventario

> Este archivo define las reglas, convenciones y buenas prácticas que cualquier agente de IA
> (Claude Code, Cursor, Copilot, etc.) o desarrollador humano DEBE seguir al trabajar en este proyecto.
> Si algo no está aquí, pregunta antes de asumir.

---

## 1. Stack tecnológico

| Capa | Tecnología | Versión | Notas |
|------|-----------|---------|-------|
| Lenguaje | Java | 21 (LTS) | LTS vigente. Spring Boot 4 soporta hasta Java 25. |
| Framework | Spring Boot | 4.0.x (actual: 4.0.4) | Basado en Spring Framework 7.x, Jakarta EE 10+. |
| Base de datos | PostgreSQL | 18 (actual: 18.3) | Última versión GA. Soporte hasta ~2030. |
| Migraciones | Flyway | 11.x (gestionado por Spring Boot 4) | Grupo: `org.flywaydb`. Requiere módulo `flyway-database-postgresql`. |
| Frontend | Angular | 21 (actual: 21.2.x) | En soporte activo. Angular 20 en LTS hasta nov 2026. |
| Build | Maven | 3.9+ | |
| Documentación API | SpringDoc OpenAPI (Swagger) | 3.x (para Spring Boot 4) | La v2.8.x es para Spring Boot 3. Para Boot 4 usar v3.x. |
| Mapeo DTO ↔ Entity | MapStruct | 1.6.3 (estable) | 1.7.0.Beta1 disponible con soporte Optional nativo. |
| Boilerplate | Lombok | 1.18.44 | Última estable a marzo 2026. |
| Tests | JUnit 5 + Testcontainers | — | Testcontainers para PostgreSQL en integración. |
| Reportes | Apache POI (Excel) + iText (PDF) | POI 5.3.x / iText 8.x | |

---

## 2. Estructura del proyecto

```
bilans-inventory/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/bilans/inventory/
    │   │   ├── config/              # Configuración Spring, Swagger, JPA Audit, Flyway
    │   │   ├── domain/
    │   │   │   ├── entity/          # Entidades JPA organizadas en subcarpetas
    │   │   │   │   ├── catalogo/    # Company, Warehouse, Unit, OperationType...
    │   │   │   │   ├── nucleo/      # Container, Operation, Reservation, LotMaster
    │   │   │   │   ├── flujo/       # Reception, Transfer, Adjustment, PickingOrder, PhysicalCount
    │   │   │   │   └── extension/   # MinMaxConfig, ShrinkageConfig, CostSnapshot...
    │   │   │   ├── enums/           # Enums tipados del dominio
    │   │   │   └── base/            # BaseEntity, AuditableEntity
    │   │   ├── repository/          # Spring Data JPA (misma subestructura que entity)
    │   │   │   └── projection/      # Interfaces de proyección para queries nativos
    │   │   ├── service/             # Servicios de dominio (lógica de negocio)
    │   │   │   ├── catalogo/
    │   │   │   ├── nucleo/
    │   │   │   ├── flujo/
    │   │   │   └── extension/
    │   │   ├── service/impl/        # Implementaciones de servicios
    │   │   │   ├── catalogo/
    │   │   │   ├── nucleo/
    │   │   │   ├── flujo/
    │   │   │   └── extension/
    │   │   ├── dto/
    │   │   │   ├── request/         # DTOs de entrada (desde Angular)
    │   │   │   └── response/        # DTOs de salida (hacia Angular)
    │   │   ├── controller/          # REST Controllers con Swagger
    │   │   ├── mapper/              # MapStruct mappers
    │   │   ├── exception/           # Excepciones de dominio + GlobalExceptionHandler
    │   │   ├── event/               # Eventos de dominio (Spring Events)
    │   │   ├── listener/            # Listeners que reaccionan a eventos
    │   │   ├── scheduler/           # Tareas programadas (@Scheduled)
    │   │   ├── integration/         # Integraciones con otros módulos
    │   │   └── util/                # Constantes y utilidades
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       └── db/migration/        # Scripts Flyway (V1__, V2__...)
    └── test/
        └── java/com/bilans/inventory/
```

### Regla de subcarpetas

Las subcarpetas dentro de `entity/`, `repository/`, `service/` y `service/impl/` siguen la misma
clasificación:

- `catalogo/` → tablas de configuración y catálogos (bodegas, unidades, tipos).
- `nucleo/` → tablas centrales del inventario (contenedores, operaciones, reservas).
- `flujo/` → tablas de documentos transaccionales (recepciones, transferencias, ajustes, picking, conteos).
- `extension/` → tablas de funcionalidad extendida (máximos/mínimos, merma, costos).

---

## 3. Arquitectura por capas — Reglas estrictas

### 3.1. Flujo de una petición HTTP

```
Angular → Controller → Service (interface) → ServiceImpl → Repository → PostgreSQL
                ↓                                    ↓
          Request DTO                          Entity JPA
                                                     ↓
                                               Response DTO
                                                     ↓
                                                 Angular
```

### 3.2. Responsabilidades por capa

#### Controller (`@RestController`)

- SOLO recibe requests, valida con `@Valid`, delega al servicio y retorna responses.
- NUNCA contiene lógica de negocio.
- NUNCA accede a un Repository directamente.
- NUNCA manipula entidades JPA; solo trabaja con DTOs.
- SIEMPRE documenta cada endpoint con anotaciones Swagger/OpenAPI.
- SIEMPRE retorna `ResponseEntity<ApiResponse<T>>` para consistencia.
- SIEMPRE usa `@Tag`, `@Operation`, `@ApiResponse` de SpringDoc.

#### Service (interface)

- Define el contrato del servicio con Javadoc claro.
- NO contiene implementación.
- Los métodos reciben DTOs de request y retornan DTOs de response (o tipos simples).
- NUNCA expone entidades JPA en la firma.

#### ServiceImpl (`@Service`)

- Implementa la interface del servicio.
- Contiene TODA la lógica de negocio, validaciones y orquestación.
- Puede llamar a otros servicios (nunca a otros controllers).
- Usa `@Transactional` donde corresponda.
- Publica eventos de dominio cuando la operación lo amerita.
- Usa los Mappers para convertir entre Entity y DTO.
- TODA operación que modifique stock DEBE pasar por `OperacionService.crearOperacion()`.

#### Repository (`JpaRepository`)

- Extiende `JpaRepository<Entity, Long>` y opcionalmente `JpaSpecificationExecutor`.
- Queries simples: usar derivación de nombre de Spring Data.
- Queries medios: usar `@Query` con JPQL.
- Queries complejos: usar `@Query(nativeQuery = true)` con SQL nativo.
- Las consultas de stock SIEMPRE usan `SUM(cantidad)` — NUNCA cargan todas las operaciones en memoria.
- Proyecciones con interfaces para queries nativos.

#### Mapper (MapStruct)

- Una interface `@Mapper` por grupo funcional.
- Convierte Entity → Response DTO y Request DTO → Entity.
- NUNCA contiene lógica de negocio.
- Usa `componentModel = "spring"` para inyección.

#### DTO

- Los DTOs de request son `record` de Java 21 con anotaciones de validación (`@NotNull`, `@Size`, etc.).
- Los DTOs de response son `record` de Java 21 sin validaciones.
- NUNCA se reutiliza un DTO de request como response ni viceversa.
- NUNCA se expone una entidad JPA directamente al controller.

---

## 4. Convenciones de código

### 4.1. Nomenclatura

| Elemento | Convención | Ejemplo |
|----------|-----------|---------|
| Clase entity | PascalCase, singular, inglés | `Container`, `Warehouse` |
| Tabla BD | snake_case, español, prefijo `inv_` | `inv_contenedores`, `inv_bodegas` |
| Columna BD | snake_case, español | `codigo_barras`, `precio_unitario` |
| Clase DTO request | PascalCase + `Request` | `CrearRecepcionRequest` |
| Clase DTO response | PascalCase + `Response` | `ContenedorStockResponse` |
| Interface service | PascalCase + `Service` | `RecepcionService` |
| Implementación | PascalCase + `ServiceImpl` | `RecepcionServiceImpl` |
| Controller | PascalCase + `Controller` | `RecepcionController` |
| Mapper | PascalCase + `Mapper` | `ContenedorMapper` |
| Enum | PascalCase, valores en UPPER_SNAKE_CASE | `TipoOperacionCodigo.RECEPCION` |
| Constantes | UPPER_SNAKE_CASE | `MAX_RESULTADOS_PAGINA` |
| Métodos | camelCase, verbo primero | `crearRecepcion()`, `obtenerStock()` |
| Paquetes | lowercase, singular | `com.bilans.inventory.service.flujo` |
| Variables | camelCase | `cantidadDisponible`, `contenedorId` |
| URLs REST | kebab-case, plural, español | `/api/v1/recepciones`, `/api/v1/bodegas` |

### 4.2. Base de datos — español

- Tablas, columnas, constraints, índices, funciones, vistas y valores de catálogo van en **español**.
- Los enums en Java usan los **mismos códigos** que están en la BD (español).
- El mapeo se resuelve con `@Table(name = "inv_contenedores")` y `@Column(name = "codigo_barras")`.

### 4.3. Java — inglés o español según contexto

- Nombres de clases, interfaces y paquetes en **inglés** (convención estándar de Spring).
- Los enums usan **español** en sus valores porque coinciden con la BD.
- Javadoc y comentarios internos pueden ser en **español**.
- Mensajes de error y logs en **español** para alinearse con el equipo.

---

## 5. Documentación Swagger / OpenAPI — Obligatoria

### 5.1. Configuración global

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API de Inventario — Bilans ERP")
                .description("Módulo de inventario: contenedores, operaciones, recepciones, "
                    + "transferencias, ajustes, picking, conteos físicos y reportes.")
                .version("1.0.0")
                .contact(new Contact().name("Equipo Bilans")))
            .addTagsItem(new Tag().name("Catálogos").description("Bodegas, ubicaciones, unidades, conversiones"))
            .addTagsItem(new Tag().name("Inventario").description("Consultas de stock y kardex"))
            .addTagsItem(new Tag().name("Recepciones").description("Ingreso de inventario"))
            .addTagsItem(new Tag().name("Transferencias").description("Traslados entre bodegas"))
            .addTagsItem(new Tag().name("Ajustes").description("Ajustes de cantidad y precio"))
            .addTagsItem(new Tag().name("Picking").description("Picking y requisiciones"))
            .addTagsItem(new Tag().name("Conteo Físico").description("Inventarios cíclicos"))
            .addTagsItem(new Tag().name("Máximos y Mínimos").description("Configuración de reabastecimiento"))
            .addTagsItem(new Tag().name("Merma").description("Registro y configuración de merma"))
            .addTagsItem(new Tag().name("Costos").description("Valorización y reportes de costo"))
            .addTagsItem(new Tag().name("Reportes").description("PDF, Excel y exportaciones"));
    }
}
```

### 5.2. Anotaciones obligatorias en cada Controller

Cada controller DEBE tener:

```java
@RestController
@RequestMapping("/api/v1/recepciones")
@Tag(name = "Recepciones", description = "Ingreso de inventario al sistema")
@RequiredArgsConstructor
public class RecepcionController {

    private final RecepcionService recepcionService;

    @PostMapping
    @Operation(
        summary = "Crear recepción de inventario",
        description = "Crea una recepción con sus líneas. Genera contenedores y operaciones positivas."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Recepción creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Bodega, ubicación o unidad no encontrada"),
        @ApiResponse(responseCode = "409", description = "Código de barras duplicado")
    })
    public ResponseEntity<ApiResponse<RecepcionResponse>> crearRecepcion(
            @Valid @RequestBody CrearRecepcionRequest request) {
        // ...
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener recepción por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Recepción encontrada"),
        @ApiResponse(responseCode = "404", description = "Recepción no encontrada")
    })
    public ResponseEntity<ApiResponse<RecepcionResponse>> obtenerRecepcion(
            @PathVariable @Parameter(description = "ID de la recepción") Long id) {
        // ...
    }
}
```

### 5.3. Anotaciones obligatorias en DTOs

Cada campo de un DTO de request DEBE tener `@Schema`:

```java
public record CrearRecepcionRequest(

    @NotNull(message = "La bodega es obligatoria")
    @Schema(description = "ID de la bodega donde se recibe", example = "1")
    Long bodegaId,

    @NotNull(message = "El tipo de recepción es obligatorio")
    @Schema(description = "Tipo: MANUAL, ORDEN_COMPRA, TRANSFERENCIA, PRODUCCION, DEVOLUCION",
            example = "MANUAL", allowableValues = {"MANUAL","ORDEN_COMPRA","TRANSFERENCIA","PRODUCCION","DEVOLUCION"})
    String tipoRecepcion,

    @Schema(description = "ID del proveedor (opcional para recepciones manuales)", example = "15")
    Long proveedorId,

    @Size(max = 500, message = "El comentario no puede exceder 500 caracteres")
    @Schema(description = "Comentario opcional", example = "Recepción de compra #OC-2025-001")
    String comentario,

    @NotEmpty(message = "Debe incluir al menos una línea")
    @Valid
    @Schema(description = "Líneas de la recepción")
    List<CrearRecepcionLineaRequest> lineas

) {}
```

Los DTOs de response también llevan `@Schema`:

```java
public record RecepcionResponse(

    @Schema(description = "ID de la recepción", example = "42")
    Long id,

    @Schema(description = "Número de recepción generado", example = "REC-20250322-0001")
    String numeroRecepcion,

    @Schema(description = "Estado actual", example = "CONFIRMADA")
    String estado,

    @Schema(description = "Fecha y hora de la recepción")
    OffsetDateTime fechaRecepcion,

    @Schema(description = "Líneas de la recepción")
    List<RecepcionLineaResponse> lineas

) {}
```

---

## 6. Patrón Service Interface + Impl

### 6.1. Interface del servicio

```java
package com.bilans.inventory.service.flujo;

/**
 * Servicio de recepción de inventario.
 * Maneja la entrada de mercadería al sistema: manual, por orden de compra,
 * por transferencia, por producción y por devolución.
 */
public interface RecepcionService {

    /**
     * Crea una recepción de inventario con sus líneas.
     * Por cada línea: crea o reutiliza contenedor, genera operación positiva.
     *
     * @param empresaId ID de la empresa
     * @param request   datos de la recepción
     * @return la recepción creada con sus líneas
     * @throws DuplicateBarcodeException si el código de barras ya existe y no se permite reutilizar
     * @throws InventoryException si bodega, ubicación o unidad no existen
     */
    RecepcionResponse crearRecepcion(Long empresaId, CrearRecepcionRequest request);

    /**
     * Obtiene una recepción por su ID.
     */
    RecepcionResponse obtenerRecepcion(Long empresaId, Long recepcionId);

    /**
     * Lista recepciones con filtros y paginación.
     */
    Page<RecepcionResponse> listarRecepciones(Long empresaId, FiltroRecepcionRequest filtro, Pageable pageable);

    /**
     * Cancela una recepción en estado BORRADOR.
     * No se puede cancelar una recepción ya confirmada.
     */
    RecepcionResponse cancelarRecepcion(Long empresaId, Long recepcionId);
}
```

### 6.2. Implementación

```java
package com.bilans.inventory.service.impl.flujo;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecepcionServiceImpl implements RecepcionService {

    private final RecepcionRepository recepcionRepository;
    private final ContenedorRepository contenedorRepository;
    // ... demás dependencias

    @Override
    public RecepcionResponse crearRecepcion(Long empresaId, CrearRecepcionRequest request) {
        // 1. Validar catálogos (bodega, ubicación, unidad)
        // 2. Crear cabecera de recepción
        // 3. Procesar cada línea (crear/reutilizar contenedor + operación)
        // 4. Publicar evento
        // 5. Mapear y retornar response
    }

    // ... demás métodos
}
```

---

## 7. Respuesta API estándar

Todas las respuestas de la API usan este wrapper:

```java
public record ApiResponse<T>(
    @Schema(description = "Indica si la operación fue exitosa")
    boolean exito,

    @Schema(description = "Mensaje descriptivo")
    String mensaje,

    @Schema(description = "Datos de la respuesta")
    T datos,

    @Schema(description = "Timestamp de la respuesta")
    OffsetDateTime timestamp
) {
    public static <T> ApiResponse<T> ok(T datos) {
        return new ApiResponse<>(true, "Operación exitosa", datos, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> ok(String mensaje, T datos) {
        return new ApiResponse<>(true, mensaje, datos, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> error(String mensaje) {
        return new ApiResponse<>(false, mensaje, null, OffsetDateTime.now());
    }
}
```

Respuesta paginada:

```java
public record PaginaResponse<T>(
    @Schema(description = "Contenido de la página")
    List<T> contenido,

    @Schema(description = "Número de página actual (base 0)")
    int pagina,

    @Schema(description = "Tamaño de página")
    int tamanio,

    @Schema(description = "Total de elementos")
    long totalElementos,

    @Schema(description = "Total de páginas")
    int totalPaginas,

    @Schema(description = "¿Es la última página?")
    boolean ultima
) {
    public static <T> PaginaResponse<T> de(Page<T> page) {
        return new PaginaResponse<>(
            page.getContent(), page.getNumber(), page.getSize(),
            page.getTotalElements(), page.getTotalPages(), page.isLast()
        );
    }
}
```

---

## 8. Manejo de excepciones global

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InventoryException.class)
    public ResponseEntity<ApiResponse<Void>> handleInventory(InventoryException ex) {
        log.warn("Error de inventario: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(DuplicateBarcodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateBarcodeException ex) {
        return ResponseEntity.status(409).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleStock(InsufficientStockException ex) {
        log.warn("Stock insuficiente: {}", ex.getMessage());
        return ResponseEntity.status(422).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(e -> errores.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest()
            .body(new ApiResponse<>(false, "Error de validación", errores, OffsetDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Error no controlado", ex);
        return ResponseEntity.status(500)
            .body(ApiResponse.error("Error interno del servidor"));
    }
}
```

### Jerarquía de excepciones

```
InventoryException (base, RuntimeException)
├── InsufficientStockException      → 422 stock insuficiente
├── ContainerNotFoundException      → 404 contenedor no encontrado
├── DuplicateBarcodeException       → 409 código de barras duplicado
├── InvalidOperationException       → 400 operación no permitida en el estado actual
├── TransferStateException          → 400 la transferencia no está en el estado correcto
├── ConversionNotFoundException     → 404 regla de conversión no encontrada
└── ReservationConflictException    → 409 conflicto con reserva existente
```

---

## 9. Regla de oro del inventario

**TODA modificación de stock DEBE pasar por `OperacionService.crearOperacion()`.**

```
Controller → ServiceImpl del flujo → OperacionService.crearOperacion() → OperationRepository.save()
```

Nunca se hace:

```java
// ❌ PROHIBIDO: escribir directamente en el repository de operaciones
operacionRepository.save(nuevaOperacion);

// ❌ PROHIBIDO: modificar stock en el contenedor
contenedor.setStock(contenedor.getStock() - cantidad);
```

Siempre se hace:

```java
// ✅ CORRECTO: toda operación pasa por el servicio central
operacionService.crearOperacion(contenedor, TipoOperacionCodigo.RECEPCION, cantidad, comentario);
```

El stock NUNCA se persiste. SIEMPRE se calcula:

```java
// ✅ CORRECTO: stock = SUM(cantidad) de operaciones activas
BigDecimal stock = operacionRepository.obtenerStockPorContenedorId(contenedorId);
```

---

## 10. Transaccionalidad

- Los servicios de flujo (`RecepcionServiceImpl`, `TransferenciaServiceImpl`, etc.)
  llevan `@Transactional` a nivel de clase o en cada método que modifica datos.
- Los servicios de consulta (`StockQueryService`) llevan `@Transactional(readOnly = true)`.
- Si una operación involucra múltiples contenedores (ej. transferencia por producto),
  TODO se hace en una sola transacción. Si algo falla, todo se revierte.
- NUNCA se hace commit parcial dentro de un flujo de negocio.

---

## 11. Eventos de dominio

Después de completar una operación importante, el servicio publica un evento:

```java
eventPublisher.publishEvent(new InventarioRecibidoEvent(this, recepcion));
```

Los listeners reaccionan de forma desacoplada:

- `MaxMinAlertListener` → recalcula máximos/mínimos del producto.
- `AuditoriaEventListener` → registra en `inv_auditoria`.
- `ContabilidadIntegrationListener` → crea partida contable si está configurado.

Los eventos NUNCA se usan para lógica que deba ser transaccional con la operación principal.
Si la partida contable es obligatoria, va dentro del mismo `@Transactional` del servicio.

---

## 12. Migraciones Flyway

- Ubicación: `src/main/resources/db/migration/`
- Formato: `V{version}__{descripcion}.sql`
- Ejemplos:
  - `V1__crear_catalogos_base.sql`
  - `V2__crear_nucleo_operativo.sql`
  - `V3__crear_flujos_negocio.sql`
  - `V4__crear_extensiones.sql`
  - `V5__crear_vistas_y_funciones.sql`
- NUNCA se modifica una migración ya aplicada. Se crea una nueva.
- `spring.jpa.hibernate.ddl-auto = validate` — Hibernate solo valida, Flyway maneja el schema.

---

## 13. URLs REST — Convenciones

```
Base: /api/v1

Catálogos:
  GET    /api/v1/bodegas                    → listar bodegas
  POST   /api/v1/bodegas                    → crear bodega
  GET    /api/v1/bodegas/{id}               → obtener bodega
  PUT    /api/v1/bodegas/{id}               → actualizar bodega
  DELETE /api/v1/bodegas/{id}               → desactivar bodega (soft delete)
  GET    /api/v1/bodegas/{id}/ubicaciones   → listar ubicaciones de una bodega

Inventario:
  GET    /api/v1/inventario/stock                     → consulta consolidada por contenedor
  GET    /api/v1/inventario/stock/producto-bodega      → stock por producto y bodega
  GET    /api/v1/inventario/kardex                     → historial de operaciones
  GET    /api/v1/inventario/contenedores/{barcode}     → stock de un barcode
  GET    /api/v1/inventario/por-vencer                 → contenedores próximos a vencer

Recepciones:
  POST   /api/v1/recepciones                → crear recepción
  GET    /api/v1/recepciones                → listar recepciones
  GET    /api/v1/recepciones/{id}           → detalle de recepción
  PATCH  /api/v1/recepciones/{id}/cancelar  → cancelar

Transferencias:
  POST   /api/v1/transferencias                      → crear transferencia
  GET    /api/v1/transferencias                      → listar
  GET    /api/v1/transferencias/{id}                 → detalle
  PATCH  /api/v1/transferencias/{id}/despachar       → despachar
  PATCH  /api/v1/transferencias/{id}/recibir         → recibir en destino
  PATCH  /api/v1/transferencias/{id}/cancelar        → cancelar

Ajustes:
  POST   /api/v1/ajustes                    → crear ajuste
  POST   /api/v1/ajustes/precio             → ajustar precio

Picking:
  POST   /api/v1/picking/ordenes            → crear orden de picking
  PATCH  /api/v1/picking/ordenes/{id}/pick  → ejecutar picking de una línea
  PATCH  /api/v1/picking/ordenes/{id}/pick-multiple → picking masivo

Conteo Físico:
  POST   /api/v1/conteos-fisicos            → crear conteo
  PATCH  /api/v1/conteos-fisicos/{id}/aplicar → aplicar ajustes del conteo

Movimientos:
  POST   /api/v1/movimientos/contenedor     → mover contenedor entre ubicaciones
  POST   /api/v1/movimientos/conversion     → convertir unidad de un contenedor

Máximos y Mínimos:
  GET    /api/v1/maximos-minimos            → listar configuraciones
  POST   /api/v1/maximos-minimos            → crear configuración
  PATCH  /api/v1/maximos-minimos/recalcular → recalcular todos

Merma:
  POST   /api/v1/merma/manual               → registrar merma manual
  GET    /api/v1/merma/estadisticas          → estadísticas de merma

Reportes:
  GET    /api/v1/reportes/inventario-costos          → reporte valorizado (PDF/Excel)
  GET    /api/v1/reportes/auxiliar-inventario         → auxiliar contable (PDF/Excel)
  GET    /api/v1/reportes/barcodes                    → generar PDF de etiquetas
```

### Reglas de URLs

- Siempre plural: `/bodegas`, `/recepciones`, `/transferencias`.
- Siempre kebab-case: `/conteos-fisicos`, `/maximos-minimos`, `/producto-bodega`.
- Acciones que no son CRUD: usar `PATCH` con verbo como sub-recurso (`/despachar`, `/recibir`, `/cancelar`).
- Filtros como query params: `?bodegaId=1&productoId=5&fechaDesde=2025-01-01`.
- Paginación: `?pagina=0&tamanio=20&ordenarPor=fechaOperacion&direccion=desc`.

---

## 14. Testing

### Estructura de tests

```
src/test/java/com/bilans/inventory/
├── unit/
│   ├── service/         → tests unitarios con mocks
│   └── mapper/          → tests de MapStruct
├── integration/
│   ├── repository/      → tests con Testcontainers PostgreSQL
│   └── controller/      → tests con @WebMvcTest o @SpringBootTest
└── e2e/
    └── flow/            → tests de flujos completos
```

### Reglas de testing

- Todo servicio de flujo debe tener tests unitarios con mocks de repositorios.
- Todo repositorio con queries nativos debe tener tests de integración con Testcontainers.
- Los flujos completos (recepción → consulta → ajuste → transferencia) deben tener tests e2e.
- Nomenclatura: `deberiaCrearRecepcionConBarcodeGenerado()`, `deberiaFallarSiStockInsuficiente()`.

---

## 15. Logging

- Usar `@Slf4j` de Lombok.
- Log INFO para operaciones exitosas: `"Recepción {} creada con {} líneas en bodega {}"`.
- Log WARN para errores de negocio: `"Stock insuficiente para contenedor {}: solicitado={}, disponible={}"`.
- Log ERROR solo para errores inesperados del sistema.
- NUNCA loguear datos sensibles (contraseñas, tokens).
- NUNCA loguear el contenido completo de una entidad — solo IDs y campos relevantes.

---

## 16. Seguridad

- Spring Security con JWT.
- Cada endpoint protegido con `@PreAuthorize` según rol.
- El `empresa_id` se obtiene del token JWT, NUNCA del request body.
- Todos los queries de datos filtran por `empresa_id` para garantizar aislamiento multi-tenant.

---

## 17. Cosas que NUNCA se deben hacer

1. ❌ Persistir stock como columna en una tabla de productos.
2. ❌ Hacer `SUM(cantidad)` en Java cargando todas las operaciones a memoria.
3. ❌ Exponer entidades JPA en controllers.
4. ❌ Poner lógica de negocio en controllers.
5. ❌ Acceder a repositorios desde controllers.
6. ❌ Crear operaciones de inventario sin pasar por `OperacionService`.
7. ❌ Usar números mágicos para tipos de operación o estados.
8. ❌ Hardcodear IDs de ubicaciones (como `ubicacion_id = 1`).
9. ❌ Modificar migraciones de Flyway ya aplicadas.
10. ❌ Dejar endpoints sin documentación Swagger.
11. ❌ Hacer commit parcial en un flujo de negocio.
12. ❌ Usar `spring.jpa.open-in-view = true` en producción.
13. ❌ Ignorar la paginación en queries que pueden retornar muchos resultados.
14. ❌ Mezclar lógica de diferentes flujos en un solo servicio (ej. recepción + ajuste).
