# AGENTS.md — Arquitectura y Estándares · Microservicio de Inventario · Exodia ERP

> **Fuente de verdad** para reglas, convenciones y decisiones arquitectónicas.
> Todo agente o desarrollador DEBE leer este documento antes de escribir código.
>
> | Documento | Propósito | Cuándo leerlo |
> |-----------|-----------|---------------|
> | `CLAUDE.md` (raíz) | Bootstrapping mínimo | Siempre (auto-cargado) |
> | `.github/AGENTS.md` (este) | Reglas y decisiones de arquitectura | Antes de escribir cualquier código |
> | `docs/inventario_arquitectura_java.md` | Código de referencia (entidades, repos, servicios) | Al implementar un componente |

---

## 1. Stack

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Lenguaje | Java | 21 (LTS) |
| Framework | Spring Boot | 4.0.x (Spring Framework 7.x, Jakarta EE 10+) |
| Base de datos | PostgreSQL | 18 |
| Migraciones | Flyway | 11.x (`flyway-database-postgresql`) |
| Frontend | Angular | 21 |
| Build | Maven | 3.9+ |
| Documentación API | SpringDoc OpenAPI | 3.x |
| Mapeo DTO ↔ Entity | MapStruct | 1.6.3 |
| Boilerplate | Lombok | 1.18.44 |
| Tests integración | Testcontainers + PostgreSQL | — |

---

## 2. Filosofía arquitectónica

### DDD pragmático + Capas limpias

Usamos **Domain-Driven Design táctico** donde agrega valor real, no como ceremonia.
El inventario es un dominio complejo con invariantes de negocio críticas (stock consistente,
trazabilidad, concurrencia), y DDD nos da herramientas concretas para protegerlas.

**Arquitectura: Microservicios.** Cada bounded context del ERP Exodia es un microservicio
independiente desplegado como su propio Spring Boot application con su propia base de datos
(database-per-service). Este repositorio ES el microservicio de inventario.

**Lo que SÍ adoptamos de DDD:**

- **Bounded Context = Microservicio** → el inventario es un servicio autónomo con su propia BD, desplegable independientemente.
- **Aggregates** → cada concepto de negocio tiene un aggregate root que protege sus invariantes.
- **Value Objects** → tipos semánticos para evitar errores con primitivos.
- **Domain Events** → comunicación desacoplada entre aggregates y hacia otros microservicios.
- **Ubiquitous Language** → todo el código usa el idioma del negocio (español).
- **Repository per Aggregate** → un repositorio por aggregate root, no por tabla.
- **Domain Services** → lógica que no pertenece a ningún aggregate específico.
- **Anti-Corruption Layer** → adaptadores para integración con otros microservicios del ERP (vía REST/mensajería).
- **Specifications** → reglas de negocio complejas encapsuladas como objetos.

**Lo que NO hacemos (por pragmatismo):**

- NO hexagonal puro (ports & adapters). Spring Boot ES nuestra infraestructura y no la abstraemos.
- NO event sourcing. El kardex de operaciones da trazabilidad sin la complejidad de ES.
- NO CQRS completo con buses. Usamos CQRS lite: servicios separados de lectura y escritura.
- NO orquestación distribuida (Saga orchestrator). Los flujos críticos son locales al microservicio. La coordinación entre servicios usa coreografía basada en eventos.

### Principio rector

> **Si una regla de negocio puede romperse en producción, debe existir
> un mecanismo en el código que lo impida. No confiamos en la disciplina del desarrollador.**

---

## 3. Bounded Context: Inventario

### 3.1. Lenguaje ubicuo

Estos términos son el vocabulario oficial. Usar EXACTAMENTE estas palabras en código, BD, logs y
conversaciones. No hay sinónimos.

| Término | Significado | NO usar |
|---------|-------------|---------|
| **Contenedor** | Unidad mínima rastreable con código de barras. Contiene una cantidad de un producto. | Pallet, caja, ítem, SKU |
| **Operación** | Movimiento en el kardex que suma o resta cantidad a un contenedor. | Transacción, movimiento (ambiguo) |
| **Recepción** | Ingreso de inventario al sistema (compra, producción, devolución). | Entrada, ingreso |
| **Transferencia** | Traslado de inventario entre bodegas. | Envío, movimiento |
| **Ajuste** | Corrección manual de cantidad o precio de un contenedor. | Modificación, cambio |
| **Picking** | Extracción de inventario para cumplir una requisición, venta u orden. | Selección, preparación |
| **Conteo físico** | Verificación real de stock vs sistema, genera ajustes automáticos. | Inventario físico, auditoría |
| **Bodega** | Ubicación física que almacena contenedores. | Almacén, depósito |
| **Ubicación** | Posición específica dentro de una bodega (pasillo, rack, nivel). | Slot, bin |
| **Lote** | Grupo de producción/importación con misma fecha de vencimiento. | Batch, partida |
| **Reserva** | Compromiso de stock para una operación futura pendiente. | Bloqueo, apartado |
| **Merma** | Pérdida de inventario por deterioro, evaporación, robo, etc. | Pérdida, desperdicio |
| **Kardex** | Registro cronológico de todas las operaciones de un contenedor/producto. | Historial, log |

### 3.2. Fronteras del bounded context

```
┌─────────────────────────────────────────────────────────┐
│            INVENTARIO (este microservicio)               │
│                                                         │
│  Contenedores · Operaciones · Stock · Recepciones       │
│  Transferencias · Ajustes · Picking · Conteos           │
│  Reservas · Merma · Códigos de barras · Valorización    │
│                                                         │
│  BD propia: PostgreSQL (database-per-service)           │
│                                                         │
│  Entrada: producto_id, proveedor_id, cliente_id,        │
│           orden_compra_id (IDs opacos — no entities)    │
│                                                         │
│  Salida: Domain Events hacia otros microservicios       │
│          API REST para Angular y otros servicios        │
└─────────────────────────────────────────────────────────┘
         │ REST/Mensajería          │ REST/Mensajería
    [ACL] ▼                    [ACL] ▼
┌──────────────┐     ┌─────────────────┐     ┌──────────────┐
│   Compras    │     │  Contabilidad   │     │  Productos   │
│ (microserv.) │     │  (microserv.)   │     │ (microserv.) │
└──────────────┘     └─────────────────┘     └──────────────┘
```

Las entidades externas (`Producto`, `Proveedor`, `Cliente`) se referencian **solo por ID** (Long).
Inventario NUNCA importa entities de otros microservicios. Si necesita datos de un producto, los recibe
como parámetro del request o los obtiene vía un adapter de integración (llamada REST al microservicio
de productos, con circuit breaker y fallback).

---

## 4. Aggregates y fronteras de consistencia

> Esta es la decisión de diseño más importante del sistema.
> Un aggregate define qué datos DEBEN ser consistentes dentro de una transacción.

### 4.1. Mapa de aggregates

| Aggregate Root | Entidades contenidas | Invariante principal |
|----------------|---------------------|----------------------|
| **Contenedor** | Contenedor, (Operacion*, Reserva*) | Stock = SUM(operaciones). Nunca negativo. |
| **Recepcion** | Recepcion, RecepcionLinea[] | Toda línea genera contenedores + operaciones vía OperacionService. |
| **Transferencia** | Transferencia, TransferenciaLinea[], TransferenciaContenedor[] | Máquina de estados: BORRADOR→DESPACHADO→RECIBIDO. |
| **Ajuste** | Ajuste, AjusteLinea[] | Cada línea genera operación +/- vía OperacionService. |
| **OrdenPicking** | OrdenPicking, PickingLinea[] | Deduce stock con lock pesimista. FEFO obligatorio. |
| **ConteoFisico** | ConteoFisico, ConteoLinea[] | Genera ajustes automáticos al aplicar resultados. |

> (*) Operacion y Reserva son entidades que pertenecen conceptualmente al aggregate de Contenedor,
> pero tienen su propio repositorio por razones de rendimiento (queries de stock masivos). Esto es
> una **concesión pragmática** documentada.

### 4.2. Reglas de los aggregates

1. **Un aggregate root por transacción.** Si un flujo toca múltiples aggregates (ej: recepción crea
   contenedores), se coordina vía Application Service con una sola transacción `@Transactional`.
2. **Los child entities se acceden SOLO a través del root.** No hay `RecepcionLineaRepository` suelto;
   las líneas se manejan a través de `Recepcion`. 
   Excepción pragmática: `OperacionRepository` y `ReservaRepository` existen por performance.
3. **Inter-aggregate references solo por ID.** Una `TransferenciaLinea` referencia `contenedorId`, 
   no contiene un `Contenedor` embebido. JPA lo mapea como `@ManyToOne(fetch = LAZY)` con FK.
4. **Validaciones de invariantes dentro del aggregate.** El aggregate root valida sus propias
   reglas antes de permitir cambios. El service orquesta, no valida.

### 4.3. Contenedor — El aggregate central

```
Contenedor (AR)
├── id, codigoBarras, productoId, bodega, ubicacion, lote
├── estado: EstadoContenedorCodigo
├── version: @Version (optimistic lock)
│
├── stock → CALCULADO: SUM(operaciones WHERE activo=true)
│           NUNCA es un campo persistido.
│
├── Operaciones (1:N, lazy) → registros inmutables del kardex
│   └── tipoOperacion, cantidad (+/-), fechaOperacion, referencia
│
└── Reservas (1:N, lazy) → compromisos pendientes
    └── cantidad, tipoReferencia, referenciaId, fechaExpiracion
```

**Invariantes del Contenedor:**
- `stock >= 0` siempre (validado en cada operación de deducción).
- `stock_disponible = stock - SUM(reservas_pendientes) >= 0`.
- `codigoBarras` es único por empresa (partial unique index con soft delete).
- Estado sigue máquina de estados: DISPONIBLE ↔ RESERVADO ↔ EN_TRANSITO → AGOTADO.

---

## 5. Value Objects

Los value objects dan **seguridad de tipos** y evitan errores con primitivos desnudos.
Se implementan como Java 21 **records** inmutables con validación en el constructor.

| Value Object | Tipo primitivo que reemplaza | Validación |
|--------------|------------------------------|------------|
| `CodigoBarras` | String | No null, no vacío, regex válido, max 50 chars |
| `Cantidad` | BigDecimal | No null, > 0 (para operaciones), >= 0 (para stock) |
| `Dinero` | BigDecimal | No null, >= 0, scale = 6 |
| `EmpresaId` | Long | No null, > 0 |
| `ProductoId` | Long | No null, > 0 |

```java
// Ejemplo de Value Object
public record Cantidad(BigDecimal valor) {
    public Cantidad {
        Objects.requireNonNull(valor, "La cantidad no puede ser nula");
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa: " + valor);
        }
    }
    public Cantidad sumar(Cantidad otra) {
        return new Cantidad(this.valor.add(otra.valor));
    }
    public boolean esMayorQue(Cantidad otra) {
        return this.valor.compareTo(otra.valor) > 0;
    }
}
```

### Mapeo JPA de Value Objects

Los VOs se mapean como `@Embeddable` o con `@Convert` (AttributeConverter) para que JPA los
persista como columnas primitivas en la tabla:

```java
@Column(name = "cantidad", precision = 18, scale = 6)
@Convert(converter = CantidadConverter.class)
private Cantidad cantidad;
```

### En DTOs

Los DTOs Request/Response usan primitivos (`BigDecimal`, `String`). La conversión VO ↔ primitivo
ocurre en el **Mapper** o en el **Application Service**, nunca en el controller.

---

## 6. Servicios: Dominio vs Aplicación

### 6.1. Domain Services

Lógica pura de negocio que **no pertenece a ningún aggregate**. No tienen anotaciones Spring.
Son clases Java puras que podrían testearse sin Spring Context.

| Domain Service | Responsabilidad |
|----------------|----------------|
| `CalculadorStock` | Lógica de cálculo: stock disponible = stock - reservas. |
| `PoliticaFEFO` | Determina el orden de contenedores para picking (First Expired, First Out). |
| `ValidadorEstadoTransferencia` | Valida transiciones de estado de la máquina de estados. |
| `CalculadorCosto` | Costo promedio ponderado, revalorización. |

```java
// Domain Service — sin anotaciones Spring
public class PoliticaFEFO {
    public List<Long> ordenarContenedoresParaPicking(List<ContenedorConStock> disponibles) {
        return disponibles.stream()
            .sorted(Comparator.comparing(ContenedorConStock::fechaVencimiento, 
                                         Comparator.nullsLast(Comparator.naturalOrder()))
                             .thenComparing(ContenedorConStock::creadoEn))
            .map(ContenedorConStock::id)
            .toList();
    }
}
```

### 6.2. Application Services

Orquestación de un caso de uso completo. Son los `@Service` de Spring. Coordinan:
aggregate root + domain services + repositories + eventos.

| Application Service | Tipo | Responsabilidad |
|-------|------|----------------|
| `OperacionService` | Comando | Crea TODA operación en el kardex. **Punto único obligatorio.** |
| `RecepcionService` | Comando | Orquesta recepción: crea contenedores, genera barcodes, publica evento. |
| `TransferenciaService` | Comando | Orquesta transferencias: transiciones de estado, despacho, recepción. |
| `AjusteInventarioService` | Comando | Orquesta ajustes: valida, crea operación, publica evento. |
| `PickingService` | Comando | Orquesta picking: resolución FEFO, lock pesimista, deducción. |
| `ConteoFisicoService` | Comando | Orquesta conteo: compara, genera ajustes automáticos. |
| `StockQueryService` | Consulta | Stock por contenedor, producto, bodega. Solo lectura. |
| `KardexQueryService` | Consulta | Historial de operaciones filtrado y paginado. Solo lectura. |
| `BarcodeService` | Comando | Genera/valida códigos de barras con secuencia por empresa. |
| `MermaService` | Comando | Merma manual y automática por reglas. |
| `ReservaService` | Comando | Crea/libera/expira reservas de stock. |

### 6.3. Patrón Interface + Impl

```
service/
  RecepcionService.java          ← Interface con Javadoc
service/impl/
  RecepcionServiceImpl.java      ← @Service @Transactional
```

El controller solo inyecta la interface. Esto permite mocking limpio en tests y future-proofing.

---

## 7. CQRS Lite — Lectura vs Escritura

No usamos buses ni event stores, pero sí separamos explícitamente las operaciones de lectura
y escritura porque tienen requisitos diferentes:

| Aspecto | Comandos (escritura) | Consultas (lectura) |
|---------|---------------------|---------------------|
| Transacción | `@Transactional` | `@Transactional(readOnly = true)` |
| Locks | Pesimista en deducción, optimista en entidades | Ninguno |
| Retorno | DTO de confirmación / void | DTOs con datos, paginados |
| Validación | Completa (negocio + estado) | Solo filtros |
| Performance | Prioridad: consistencia | Prioridad: velocidad |

### Servicios de consulta

Los servicios de consulta (`StockQueryService`, `KardexQueryService`) pueden:
- Usar **queries nativos** optimizados.
- Devolver **proyecciones** (`@Value` interfaces, records) en vez de entities completas.
- Bypassear el aggregate root y leer directamente de repositories de lectura.

```java
// ✅ Correcto para consultas — query nativo optimizado
@Query(value = """
    SELECT c.id, c.codigo_barras, COALESCE(SUM(o.cantidad), 0) as stock
    FROM inv_contenedores c
    LEFT JOIN inv_operaciones o ON o.contenedor_id = c.id AND o.activo = true
    WHERE c.producto_id = :productoId AND c.bodega_id = :bodegaId AND c.activo = true
    GROUP BY c.id
    HAVING COALESCE(SUM(o.cantidad), 0) > 0
    ORDER BY c.fecha_vencimiento ASC NULLS LAST
    """, nativeQuery = true)
List<ContenedorStockProjection> findContenedoresConStockPorProductoYBodega(
    @Param("productoId") Long productoId,
    @Param("bodegaId") Long bodegaId);
```

---

## 8. Estructura del proyecto

```
src/main/java/com/exodia/inventario/
│
├── config/                        → Configuración técnica Spring
│   ├── OpenApiConfig.java
│   ├── JpaAuditConfig.java
│   ├── SecurityConfig.java
│   └── InventarioConfig.java      → Beans de Domain Services
│
├── domain/                        → CAPA DE DOMINIO (el corazón)
│   ├── base/                      → BaseEntity, AuditableEntity
│   ├── modelo/                    → Entities organizadas por aggregate
│   │   ├── catalogo/              → Empresa, Bodega, Ubicacion, Unidad, Conversion,
│   │   │                            TipoOperacion, TipoAjuste, EstadoContenedor, EstadoTransferencia
│   │   ├── contenedor/            → Contenedor (AR), Operacion, Reserva, Lote
│   │   ├── recepcion/             → Recepcion (AR), RecepcionLinea
│   │   ├── transferencia/         → Transferencia (AR), TransferenciaLinea, TransferenciaContenedor
│   │   ├── ajuste/                → Ajuste (AR), AjusteLinea
│   │   ├── picking/               → OrdenPicking (AR), PickingLinea
│   │   ├── conteo/                → ConteoFisico (AR), ConteoLinea
│   │   └── extension/             → MaximoMinimo, ConfigMerma, RegistroMerma,
│   │                                FotoCosto, SecuenciaBarcode, Auditoria
│   ├── vo/                        → Value Objects: CodigoBarras, Cantidad, Dinero, EmpresaId, ProductoId
│   ├── enums/                     → Enums tipados (TipoOperacionCodigo, EstadoContenedorCodigo, etc.)
│   ├── evento/                    → Domain Events
│   ├── servicio/                  → Domain Services puros (sin Spring)
│   │   ├── CalculadorStock.java
│   │   ├── PoliticaFEFO.java
│   │   ├── ValidadorEstadoTransferencia.java
│   │   └── CalculadorCosto.java
│   └── politica/                  → Specifications / Business rules
│       ├── PoliticaDeduccionStock.java
│       └── PoliticaReserva.java
│
├── aplicacion/                    → CAPA DE APLICACIÓN (orquestación)
│   ├── comando/                   → Application Services de escritura
│   │   ├── OperacionService.java / impl/OperacionServiceImpl.java
│   │   ├── RecepcionService.java / impl/RecepcionServiceImpl.java
│   │   ├── TransferenciaService.java / impl/TransferenciaServiceImpl.java
│   │   ├── AjusteInventarioService.java / impl/AjusteInventarioServiceImpl.java
│   │   ├── PickingService.java / impl/PickingServiceImpl.java
│   │   ├── ConteoFisicoService.java / impl/ConteoFisicoServiceImpl.java
│   │   ├── BarcodeService.java / impl/BarcodeServiceImpl.java
│   │   ├── ReservaService.java / impl/ReservaServiceImpl.java
│   │   └── MermaService.java / impl/MermaServiceImpl.java
│   └── consulta/                  → Application Services de lectura
│       ├── StockQueryService.java / impl/StockQueryServiceImpl.java
│       └── KardexQueryService.java / impl/KardexQueryServiceImpl.java
│
├── repositorio/                   → Spring Data JPA Repositories
│   ├── catalogo/                  → EmpresaRepository, BodegaRepository, etc.
│   ├── contenedor/                → ContenedorRepository, OperacionRepository, ReservaRepository, LoteRepository
│   ├── recepcion/                 → RecepcionRepository
│   ├── transferencia/             → TransferenciaRepository
│   ├── ajuste/                    → AjusteRepository
│   ├── picking/                   → OrdenPickingRepository
│   ├── conteo/                    → ConteoFisicoRepository
│   ├── extension/                 → MaximoMinimoRepository, etc.
│   └── proyeccion/                → ContenedorStockProjection, ProductoBodegaStockProjection, etc.
│
├── interfaz/                      → CAPA DE INTERFAZ (API)
│   ├── rest/                      → REST Controllers (subcarpetas por dominio)
│   │   ├── catalogo/
│   │   ├── inventario/            → Stock, Kardex
│   │   ├── recepcion/
│   │   ├── transferencia/
│   │   ├── ajuste/
│   │   ├── picking/
│   │   ├── conteo/
│   │   └── reporte/
│   ├── dto/
│   │   ├── peticion/              → Request DTOs (Java records + @Valid + @Schema)
│   │   └── respuesta/             → Response DTOs (Java records + @Schema)
│   └── mapeador/                  → MapStruct mappers
│
├── infraestructura/               → CAPA DE INFRAESTRUCTURA
│   ├── listener/                  → Event listeners (Spring)
│   ├── programacion/              → @Scheduled tasks
│   └── integracion/               → Anti-Corruption Layer hacia otros microservicios
│       ├── compras/
│       ├── ventas/
│       ├── produccion/
│       └── contabilidad/
│
├── excepcion/                     → Excepciones de dominio + GlobalExceptionHandler
│   ├── InventarioException.java
│   ├── StockInsuficienteException.java
│   ├── BarcodeDuplicadoException.java
│   ├── ContenedorNoEncontradoException.java
│   ├── OperacionInvalidaException.java
│   ├── EstadoTransferenciaException.java
│   ├── ConversionNoEncontradaException.java
│   ├── ConflictoReservaException.java
│   └── handler/
│       └── GlobalExceptionHandler.java
│
└── util/                          → Constantes, helpers
    └── InventarioConstantes.java
```

### Decisiones clave de la estructura

1. **`domain/modelo/` organizado por aggregate**, no por capa técnica. Cuando abres `contenedor/`
   ves TODO lo que forma ese aggregate: root + child entities.
2. **`domain/servicio/` y `domain/politica/`** son clases Java puras sin Spring. Testeables sin contexto.
3. **`aplicacion/comando/` vs `aplicacion/consulta/`** — CQRS lite en la estructura.
4. **`interfaz/`** agrupa todo lo que toca HTTP: controllers, DTOs, mappers.
5. **`infraestructura/integracion/`** es la Anti-Corruption Layer.
6. **`repositorio/`** agrupa por aggregate, alineado con `domain/modelo/`.

---

## 9. Nomenclatura

| Elemento | Convención | Ejemplo |
|----------|-----------|---------|
| Paquete Java | lowercase, singular | `com.exodia.inventario.aplicacion.comando` |
| Entity (AR) | PascalCase, singular | `Contenedor`, `Recepcion` |
| Entity (child) | PascalCase, singular | `RecepcionLinea`, `TransferenciaContenedor` |
| Value Object | PascalCase, singular | `CodigoBarras`, `Cantidad`, `Dinero` |
| Domain Service | PascalCase, descriptivo | `PoliticaFEFO`, `CalculadorStock` |
| Domain Event | PascalCase + `Event` | `InventarioRecibidoEvent`, `StockAjustadoEvent` |
| Application Service | PascalCase + `Service` | `RecepcionService` |
| Service Impl | PascalCase + `ServiceImpl` | `RecepcionServiceImpl` |
| Controller | PascalCase + `Controller` | `RecepcionController` |
| Repository | PascalCase + `Repository` | `ContenedorRepository` |
| Projection | PascalCase + `Projection` | `ContenedorStockProjection` |
| Mapper | PascalCase + `Mapper` | `ContenedorMapper` |
| DTO request | Verbo + Nombre + `Request` | `CrearRecepcionRequest` |
| DTO response | Nombre + `Response` | `ContenedorStockResponse` |
| Enum | PascalCase, valores UPPER_SNAKE | `TipoOperacionCodigo.RECEPCION` |
| Specification | PascalCase + `Politica` | `PoliticaDeduccionStock` |
| Tabla BD | `inv_` + snake_case, plural, español | `inv_contenedores` |
| Columna BD | snake_case, español | `codigo_barras`, `precio_unitario` |
| Índice BD | `idx_` + tabla_singular + columnas | `idx_operacion_contenedor`, `idx_contenedor_fefo` |
| Unique BD | `uq_` + tabla_singular + columnas | `uq_contenedor_empresa_codigo_barras` |
| FK BD | `fk_` + tabla_singular_origen + destino | `fk_bodega_ubicacion_standby` |
| URL REST | kebab-case, plural, español | `/api/v1/recepciones` |
| Método | camelCase, verbo primero | `crearRecepcion()`, `obtenerStock()` |
| Constante | UPPER_SNAKE_CASE | `MAX_RESULTADOS_PAGINA` |

### Idioma

- **BD completa en español**: tablas, columnas, constraints, funciones, valores de catálogo.
- **Java en español**: clases, paquetes, métodos — alineados al lenguaje ubicuo.
- **Enums**: valores en español, coinciden exactamente con la BD (`RECEPCION`, `DISPONIBLE`).
- **Javadoc, logs, mensajes de error**: español.
- **Nombres técnicos se mantienen en inglés**: `Repository`, `Service`, `Controller`, `Event`, `Projection`.

---

## 10. Reglas de oro del inventario

### 10.1. Stock = cálculo, nunca dato persistido

```sql
SELECT COALESCE(SUM(cantidad), 0) FROM inv_operaciones
WHERE contenedor_id = :id AND activo = true;
```

Jamás crear columnas `stock_actual`. Excepción: `inv_maximos_minimos.stock_actual_calculado`
recalculado por servicio, nunca editable directamente.

### 10.2. Toda operación pasa por OperacionService

```java
// ✅ SIEMPRE
operacionService.crearOperacion(contenedor, TipoOperacionCodigo.RECEPCION, cantidad, comentario);

// ❌ NUNCA
operacionRepository.save(nuevaOperacion);
```

`OperacionService` es el **único punto de entrada** al kardex. Es responsable de:
- Validar que la operación es consistente.
- Verificar stock suficiente (en operaciones de deducción, con lock pesimista).
- Persistir la operación.
- Publicar el domain event correspondiente.

### 10.3. Operaciones son inmutables

Una vez creada, una `Operacion` NUNCA se modifica ni se elimina. Para revertir, se crea
una operación contraria (ej: `AJUSTE_NEGATIVO` revierte un `AJUSTE_POSITIVO`).
El soft delete (`activo = false`) solo se usa en escenarios excepcionales de auditoría.

---

## 11. Concurrencia e integridad de datos

> En un sistema de inventario multi-usuario, las condiciones de carrera no son teóricas.
> Dos pickers pueden intentar deducir del mismo contenedor simultáneamente.
> Esta sección es **no negociable**.

### 11.1. Locking optimista — entidades modificables

Toda entidad que pueda ser modificada concurrentemente DEBE tener `@Version`:

```java
@Version
@Column(name = "version")
private Long version;
```

**Aplica a:** `Contenedor`, `Reserva`, `Transferencia`, `OrdenPicking`, `ConteoFisico`.

`OptimisticLockException` → HTTP 409 + `INV-008` + `"Registro modificado por otro usuario. Reintente."`.

### 11.2. Locking pesimista — deducción de stock

Al deducir stock (picking, transferencia salida, ajuste negativo), se DEBE bloquear el contenedor:

```java
@Query("SELECT c FROM Contenedor c WHERE c.id = :id")
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Contenedor> findByIdForUpdate(@Param("id") Long id);
```

**Flujo seguro de deducción:**
1. `findByIdForUpdate(id)` — bloquea la fila en PostgreSQL.
2. `stockQueryService.obtenerStockContenedor(id)` — calcula stock real.
3. Valida: `stock_disponible >= cantidad_solicitada`.
4. `operacionService.crearOperacion(...)` — registra la deducción.
5. Commit — libera el lock automáticamente.

### 11.3. Stock disponible vs stock total

```
stock_total      = SUM(operaciones activas del contenedor)
stock_reservado  = SUM(reservas pendientes del contenedor)
stock_disponible = stock_total - stock_reservado
```

Antes de deducir, SIEMPRE verificar `stock_disponible`, no `stock_total`.
Si `stock_disponible < cantidad` → `StockInsuficienteException` (`INV-001`).

### 11.4. Orden de locks

Si un flujo necesita bloquear múltiples contenedores (transferencia con varias líneas),
bloquear SIEMPRE en **orden ascendente por ID** para evitar deadlocks:

```java
List<Long> ids = contenedorIds.stream().sorted().toList();
for (Long id : ids) {
    contenedorRepository.findByIdForUpdate(id);
}
```

---

## 12. Máquinas de estado

### 12.1. Contenedor

```
                    ┌───────────┐
         ┌─────────│ DISPONIBLE│◄────────────┐
         │         └───────────┘             │
         │              │                    │
    (reservar)     (despachar)         (recibir / liberar)
         │              │                    │
         ▼              ▼                    │
   ┌──────────┐   ┌────────────┐      ┌─────────────┐
   │ RESERVADO│   │ EN_TRANSITO│──────│EN_STANDBY   │
   └──────────┘   └────────────┘      └─────────────┘
         │
    (agotar stock)
         │
         ▼
   ┌──────────┐   ┌────────────┐      ┌─────────────┐
   │ AGOTADO  │   │ CUARENTENA │      │  BLOQUEADO  │
   └──────────┘   └────────────┘      └─────────────┘
```

### 12.2. Transferencia

```
BORRADOR → CONFIRMADO → DESPACHADO → EN_TRANSITO → RECIBIDO_PARCIAL → RECIBIDO_COMPLETO
                    │                                                         │
                    └──→ CANCELADO ◄──────────────────────────────────────────┘
                                                                    CIERRE_FORZADO
```

Las transiciones se validan en el Domain Service `ValidadorEstadoTransferencia`.
Cualquier transición inválida lanza `EstadoTransferenciaException` (`INV-005`).

---

## 13. Domain Events

### 13.1. Catálogo de eventos

| Evento | Publicado por | Datos clave |
|--------|--------------|-------------|
| `InventarioRecibidoEvent` | RecepcionService | recepcionId, líneas, bodegaId |
| `StockAjustadoEvent` | AjusteInventarioService | ajusteId, contenedorId, cantidadAnterior, cantidadNueva |
| `TransferenciaDespachadadEvent` | TransferenciaService | transferenciaId, bodegaOrigen, bodegaDestino |
| `TransferenciaRecibidaEvent` | TransferenciaService | transferenciaId, contenedoresRecibidos |
| `PickingCompletadoEvent` | PickingService | ordenPickingId, líneasProcesadas |
| `StockBajoMinimoEvent` | MaximosMinimosService | productoId, bodegaId, stockActual, minimo |
| `ContenedorPorVencerEvent` | ExpirationAlertJob | contenedorId, fechaVencimiento, diasRestantes |
| `ConteoAplicadoEvent` | ConteoFisicoService | conteoId, ajustesGenerados |

### 13.2. Reglas de eventos

- **Eventos críticos** (auditoría, integridad): procesar DENTRO de la transacción → `@EventListener`.
- **Eventos no-críticos** (notificaciones, recálculos): procesar DESPUÉS del commit →
  `@TransactionalEventListener(phase = AFTER_COMMIT)`.
- Todos los eventos son **inmutables** (records de Java).
- Un evento NUNCA dispara otro flujo de escritura que toque el mismo aggregate (evita ciclos).

### 13.3. Listeners

| Listener | Evento | Acción | Fase |
|----------|--------|--------|------|
| `AuditoriaEventListener` | Todos | Registra en `inv_auditoria` | WITHIN TX |
| `MaxMinAlertListener` | InventarioRecibido, StockAjustado | Recalcula máx/mín | AFTER COMMIT |
| `ContabilidadIntegrationListener` | InventarioRecibido, StockAjustado | Publica a contabilidad | AFTER COMMIT |

---

## 14. Arquitectura por capas — Reglas estrictas

```
  ┌─────────────────────────────────────────────────────┐
  │             INTERFAZ (rest, dto, mapeador)           │
  │  Recibe HTTP, valida @Valid, delega, retorna DTO    │
  ├─────────────────────────────────────────────────────┤
  │         APLICACIÓN (comando, consulta)               │
  │  Orquesta: repo + domain service + eventos          │
  │  @Transactional aquí                                │
  ├─────────────────────────────────────────────────────┤
  │              DOMINIO (modelo, vo, servicio, evento)  │
  │  Entidades, VOs, lógica pura. SIN Spring.           │
  ├─────────────────────────────────────────────────────┤
  │      INFRAESTRUCTURA (repos, listeners, integ.)     │
  │  Implementaciones técnicas, adapters                │
  └─────────────────────────────────────────────────────┘
```

| Capa | Depende de | NUNCA depende de |
|------|-----------|-------------------|
| **Interfaz** | Aplicación, DTOs | Dominio directamente, Repositorios |
| **Aplicación** | Dominio, Repositorios (interfaces) | Interfaz |
| **Dominio** | Nada (es el centro) | Aplicación, Interfaz, Infraestructura, Spring |
| **Infraestructura** | Dominio, Spring, JPA | Interfaz, Aplicación |

### Reglas por capa

| Capa | DEBE | NUNCA |
|------|------|-------|
| **Controller** | `@Valid` en requests, delegar a Application Service, retornar `ApiResponse<T>` | Lógica de negocio, acceso a Repository, manipular entities |
| **Application Service** | `@Transactional`, orquestar repos + domain services, publicar eventos | Exponer entidades JPA a la capa de interfaz |
| **Domain Service** | Lógica pura, recibir entities/VOs, retornar entities/VOs | Usar `@Service`, `@Autowired`, acceder a repositorios |
| **Domain Entity** | Validar sus propias invariantes, exponer métodos de negocio | Tener lógica de presentación o de persistencia |
| **Repository** | `JpaRepository<E, Long>`, queries derivados, `@Query` JPQL/nativo | Contener lógica de negocio |
| **Mapper** | `@Mapper(componentModel = "spring")`. Entity ↔ DTO. | Lógica de negocio |

---

## 15. Base de datos — Convenciones

### 15.1. Idioma, prefijo y esquema

Todo en español. Tablas prefijadas con `inv_`. Esquema: `public` (default PostgreSQL).

### 15.2. Soft delete + Partial unique indexes

- `activo BOOLEAN NOT NULL DEFAULT true` en toda tabla de negocio.
- Queries de negocio **siempre** filtran `WHERE activo = true`.
- Unique constraints DEBEN ser **partial indexes** para funcionar con soft delete:

```sql
CREATE UNIQUE INDEX uq_contenedor_empresa_codigo_barras
    ON inv_contenedores (empresa_id, codigo_barras)
    WHERE activo = true;
```

### 15.3. Estrategia de indexación

| Tipo | Cuándo | Naming |
|------|--------|--------|
| PK | Automático (`id BIGSERIAL`) | — |
| FK | Toda FK. PostgreSQL NO los crea automáticamente. | `idx_{tabla}_{columna_fk}` |
| Stock queries | `inv_operaciones(contenedor_id, activo)` y `(empresa_id, producto_id, bodega_id, activo)` | `idx_operacion_contenedor`, `idx_operacion_stock_por_producto_bodega` |
| Barcode | Partial unique `inv_contenedores(empresa_id, codigo_barras) WHERE activo = true` | `uq_contenedor_empresa_codigo_barras` |
| FEFO | `inv_contenedores(producto_id, bodega_id, fecha_vencimiento, creado_en) WHERE activo = true` | `idx_contenedor_fefo` |
| Kardex | `inv_operaciones(empresa_id, fecha_operacion DESC, id DESC)` | `idx_operacion_kardex` |

### 15.4. Migraciones Flyway

- Ubicación: `src/main/resources/db/migration/`
- Formato: `V{version}__{descripcion}.sql` (ejemplo: `V001__crear_catalogos.sql`)
- **NUNCA** modificar una migración existente. Crear una nueva.
- Versionado semántico: `V001`–`V099` catálogos, `V100`–`V199` núcleo, `V200`+ flujos.
- `spring.jpa.hibernate.ddl-auto = validate`

### 15.5. Auditoría de columnas

Toda tabla de negocio incluye:

```sql
creado_en      TIMESTAMPTZ NOT NULL DEFAULT now(),
creado_por     BIGINT,
modificado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),
modificado_por BIGINT,
activo         BOOLEAN NOT NULL DEFAULT true,
version        BIGINT NOT NULL DEFAULT 0
```

JPA: `@EntityListeners(AuditingEntityListener.class)`, `@CreatedDate`, `@LastModifiedDate`,
`@CreatedBy`, `@LastModifiedBy` (via `AuditorAware<Long>` que lee el JWT).

---

## 16. API REST

### 16.1. Convenciones URL

```
Base: /api/v1
Estilo: plural, kebab-case, español.
Acciones no-CRUD: PATCH con verbo como sub-recurso.
```

```
GET    /api/v1/inventario/stock/producto-bodega
GET    /api/v1/inventario/kardex
POST   /api/v1/recepciones
GET    /api/v1/recepciones/{id}
PATCH  /api/v1/transferencias/{id}/despachar
PATCH  /api/v1/transferencias/{id}/recibir
POST   /api/v1/conteos-fisicos
PATCH  /api/v1/conteos-fisicos/{id}/aplicar
POST   /api/v1/ordenes-picking
PATCH  /api/v1/ordenes-picking/{id}/ejecutar
```

### 16.2. Respuesta estándar

```java
ResponseEntity<ApiResponse<T>>                    // objeto único
ResponseEntity<ApiResponse<PaginaResponse<T>>>     // listado paginado
```

`ApiResponse<T>`: `exito`, `mensaje`, `datos`, `timestamp`, `codigoError` (nullable).

### 16.3. Códigos de error de dominio

| Código | HTTP | Significado |
|--------|------|-------------|
| `INV-001` | 422 | Stock insuficiente |
| `INV-002` | 409 | Código de barras duplicado |
| `INV-003` | 404 | Contenedor no encontrado |
| `INV-004` | 400 | Operación no permitida en estado actual |
| `INV-005` | 400 | Transición de estado inválida |
| `INV-006` | 404 | Regla de conversión no encontrada |
| `INV-007` | 409 | Conflicto con reserva existente |
| `INV-008` | 409 | Registro modificado por otro usuario |
| `INV-009` | 400 | Error de validación de campos |
| `INV-010` | 404 | Entidad no encontrada (genérico) |
| `INV-011` | 400 | Cantidad inválida |
| `INV-012` | 409 | Contenedor en estado no operable |

Jerarquía:

```
InventarioException (RuntimeException) → codigoError + httpStatus
├── StockInsuficienteException         → INV-001 / 422
├── BarcodeDuplicadoException          → INV-002 / 409
├── ContenedorNoEncontradoException    → INV-003 / 404
├── OperacionInvalidaException         → INV-004 / 400
├── EstadoTransferenciaException       → INV-005 / 400
├── ConversionNoEncontradaException    → INV-006 / 404
├── ConflictoReservaException          → INV-007 / 409
├── EntidadNoEncontradaException       → INV-010 / 404
└── ContenedorNoOperableException      → INV-012 / 409
```

### 16.4. Swagger / OpenAPI

Todo controller: `@Tag`, `@Operation`, `@ApiResponses`, `@Parameter`.
Todo DTO: `@Schema` en cada campo.

### 16.5. Paginación

- Parámetros: `?pagina=0&tamanio=20&ordenarPor=fechaOperacion&direccion=desc`
- **Límite duro**: `tamanio` máximo = 100.
- Respuesta siempre con `PaginaResponse<T>`.

---

## 17. Performance

### 17.1. N+1 queries

- Relaciones con `FetchType.LAZY` por defecto.
- Si necesitas relaciones: `@EntityGraph` o `JOIN FETCH`.
- **NUNCA** acceder a lazy collection dentro de un loop.

### 17.2. Batch inserts

```yaml
spring.jpa.properties.hibernate.jdbc.batch_size: 50
spring.jpa.properties.hibernate.order_inserts: true
spring.jpa.properties.hibernate.order_updates: true
```

### 17.3. Stock queries

**SIEMPRE** con `SUM(cantidad)` en BD. NUNCA cargar operaciones a memoria.
Los índices de §15.3 garantizan index scans.

### 17.4. Connection pool

```yaml
spring.datasource.hikari:
  maximum-pool-size: 20
  minimum-idle: 5
  connection-timeout: 30000
  idle-timeout: 600000
  max-lifetime: 1800000
```

### 17.5. Open-in-view deshabilitado

```yaml
spring.jpa.open-in-view: false
```

Esto fuerza a resolver todas las relaciones dentro de `@Transactional`. Si hay un lazy loading
fuera de transacción, falla rápidamente en desarrollo en vez de generar N+1 silenciosos en producción.

---

## 18. Transaccionalidad

- **Application Services de escritura**: `@Transactional`
- **Application Services de lectura**: `@Transactional(readOnly = true)`
- Un flujo completo = una transacción. Si algo falla, todo se revierte.
- **Domain Events críticos** (auditoría): DENTRO de la transacción.
- **Domain Events no-críticos** (notificaciones): `@TransactionalEventListener(phase = AFTER_COMMIT)`.

---

## 19. Seguridad

- Spring Security con JWT.
- `empresa_id` del token JWT. **NUNCA** del request body ni path.
- Todo query filtra por `empresa_id` → **aislamiento multi-tenant obligatorio**.
- `@PreAuthorize` por rol en cada endpoint.
- **CORS**: explícito por entorno. Jamás `allowedOrigins("*")` en producción.

---

## 20. Observabilidad

### 20.1. Logging

- `@Slf4j` (Lombok).
- **INFO**: operaciones exitosas → `"Recepción {} creada con {} líneas en bodega {}"`.
- **WARN**: errores de negocio → `"Stock insuficiente para contenedor {}: solicitado={}, disponible={}"`.
- **ERROR**: solo errores inesperados del sistema.
- **NUNCA** loguear entidades completas ni datos sensibles. Solo IDs y campos relevantes.

### 20.2. Actuator + Metrics

- Endpoints: `health`, `info`, `metrics`, `prometheus`.
- Health check custom para PostgreSQL.
- Métricas de dominio:
  - `inventario.operaciones.creadas` (counter, tag: tipoOperacion)
  - `inventario.stock.consultas` (counter)
  - `inventario.recepciones.procesadas` (counter)

### 20.3. Audit trail

Toda operación registrada en `inv_auditoria` vía `AuditoriaEventListener`:
quién, cuándo, qué entidad, cuál fue el cambio.

---

## 21. Anti-Corruption Layer (Integraciones)

Las integraciones con otros microservicios del ERP (Compras, Ventas, Producción, Contabilidad, Productos)
se implementan como **adapters** en `infraestructura/integracion/`.

### Principio: comunicación inter-servicio

- **Síncrona (REST)**: para consultas que necesitan respuesta inmediata (ej: obtener nombre de producto).
  Usar `RestClient` de Spring 7 con circuit breaker (Resilience4j) y timeout.
- **Asíncrona (eventos)**: para notificaciones que no necesitan respuesta (ej: inventario recibido → contabilidad).
  Los eventos se publican localmente y un outbox/listener los envía al broker.
- **Regla de oro**: si el servicio externo cae, el inventario DEBE seguir operando.
  Los adapters retornan fallbacks seguros o cachean datos mínimos.

### Reglas de integración

1. **Inventario NUNCA importa entities de otros microservicios.** Solo IDs (Long).
2. Si necesita datos de un producto, recibe un DTO o usa un adapter que encapsula la llamada REST.
3. Las integraciones usan **interfaces** definidas en `aplicacion/`. La implementación
   concreta vive en `infraestructura/integracion/`.
4. Si el microservicio externo no responde, el adapter retorna un fallback seguro o lanza excepción controlada.
5. **Circuit breaker** obligatorio en toda llamada síncrona a otro microservicio.
6. **Timeout**: máximo 3 segundos para llamadas síncronas inter-servicio.

```java
// Interface en aplicacion/
public interface ProductoAdapter {
    Optional<ProductoInfoDto> obtenerProducto(Long productoId);
    String obtenerNombreProducto(Long productoId);
}

// Implementación en infraestructura/integracion/
@Component
public class ProductoAdapterImpl implements ProductoAdapter {
    private final RestClient restClient;
    // Llama al microservicio de productos vía REST con circuit breaker
}
```

---

## 22. Testing

```
src/test/java/com/exodia/inventario/
├── unit/
│   ├── dominio/           → Domain services, VOs, policies (sin Spring)
│   ├── aplicacion/        → Application services con mocks
│   └── mapeador/          → MapStruct mappers
├── integration/
│   ├── repositorio/       → Testcontainers PostgreSQL
│   └── rest/              → @WebMvcTest o @SpringBootTest
└── e2e/
    └── flujo/             → Flujos completos end-to-end
```

| Tipo | Qué se testea | Cómo |
|------|---------------|------|
| Unit — Domain | VOs, Domain Services, Policies | JUnit 5 puro. Sin Spring. Sin mocks. |
| Unit — Application | Application Services | Mockito para repos y domain services |
| Unit — Mapper | MapStruct mappings | Spring test context mínimo |
| Integration — Repo | Queries nativos, stock queries | Testcontainers PostgreSQL |
| Integration — REST | Controllers end-to-end | `@SpringBootTest` + TestRestTemplate |
| E2E — Flujo | recepción→stock→ajuste→transferencia→picking | Testcontainers, flujo completo |

### Nomenclatura de tests

```java
@Test
void deberiaCrearRecepcionConBarcodeGenerado() { }

@Test
void deberiaFallarConStockInsuficienteAlDeducir() { }

@Test
void deberiaOrdenarContenedoresPorFEFOparaPicking() { }
```

### Base de datos en tests

**Testcontainers PostgreSQL exclusivamente.** No H2. Los queries nativos y partial unique indexes
de PostgreSQL no son compatibles con H2.

---

## 23. Anti-patrones — Lista negra

| # | Prohibido | Causa |
|---|-----------|-------|
| 1 | Persistir stock como columna | Se desincroniza. Stock = `SUM(operaciones)`. |
| 2 | `SUM(cantidad)` en Java (cargar a memoria) | O(n) en memoria vs O(1) en BD con índice. |
| 3 | Exponer entities en controllers | Acoplamiento, lazy loading fuera de transacción. |
| 4 | Lógica de negocio en controllers | Viola capas. Controller solo delega. |
| 5 | Repository desde controller | Controller → Service → Repository. |
| 6 | `operacionRepository.save()` directo | Toda operación pasa por `OperacionService`. |
| 7 | Números mágicos para tipos/estados | Usar enums tipados o catálogos. |
| 8 | Hardcodear IDs (`ubicacion_id = 1`) | Resolver por código o query. |
| 9 | Modificar migraciones Flyway | Crear nueva migración. |
| 10 | Endpoint sin Swagger | Todo endpoint documentado. |
| 11 | Commit parcial en flujo | Una transacción = un flujo completo. |
| 12 | `open-in-view = true` | N+1 silenciosos en producción. |
| 13 | Paginación sin límite | `tamanio` máximo = 100. |
| 14 | Mezclar flujos en un servicio | Un servicio por caso de uso / aggregate. |
| 15 | Deducir stock sin lock pesimista | Condición de carrera → double-spending. |
| 16 | `allowedOrigins("*")` en producción | CSRF abierto. |
| 17 | Lógica de Spring en Domain Services | `domain/servicio/` es Java puro. |
| 18 | Importar entities de otros microservicios | Solo IDs. Anti-Corruption Layer via REST/eventos. |
| 19 | Domain Event que modifica mismo aggregate | Ciclos y side-effects invisibles. |
| 20 | Value Object mutable | VOs son records inmutables. |
| 21 | Test de integración con H2 | Incompatible con queries nativos PostgreSQL. |

