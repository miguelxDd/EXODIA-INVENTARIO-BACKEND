Plan to implement                                                                                  │
│                                                                                                    │
│ Plan de Implementacion por Fases — Microservicio de Inventario Exodia                              │
│                                                                                                    │
│ Contexto                                                                                           │
│                                                                                                    │
│ El microservicio tiene una base solida: 91 archivos Java (entidades, enums, repos, configs,        │
│ excepciones), 20 migraciones Flyway, y documentacion completa (AGENTS.md, arquitectura, DDL). Sin  │
│ embargo, 0% de logica de negocio, 0% API REST, 0% infraestructura, 0% tests estan implementados.   │
│ Se necesita un plan incremental donde cada fase entregue funcionalidad demostrable.                │
│                                                                                                    │
│ Estado actual                                                                                      │
│                                                                                                    │
│ Capa: Fundacion (pom, configs, migrations)                                                         │
│ %: 95%                                                                                             │
│ Detalle: Listo                                                                                     │
│ ────────────────────────────────────────                                                           │
│ Capa: Dominio (entidades + enums)                                                                  │
│ %: 30%                                                                                             │
│ Detalle: Esqueleto. Faltan VOs, servicios de dominio, eventos, politicas                           │
│ ────────────────────────────────────────                                                           │
│ Capa: Repositorios                                                                                 │
│ %: 80%                                                                                             │
│ Detalle: 20 repos definidos, queries nativos de stock/FEFO ya escritos                             │
│ ────────────────────────────────────────                                                           │
│ Capa: Logica de negocio                                                                            │
│ %: 0%                                                                                              │
│ Detalle: Cero servicios de aplicacion                                                              │
│ ────────────────────────────────────────                                                           │
│ Capa: API REST                                                                                     │
│ %: 0%                                                                                              │
│ Detalle: Cero controllers, DTOs, mappers                                                           │
│ ────────────────────────────────────────                                                           │
│ Capa: Infraestructura                                                                              │
│ %: 0%                                                                                              │
│ Detalle: Cero listeners, schedulers, integraciones                                                 │
│ ────────────────────────────────────────                                                           │
│ Capa: Tests                                                                                        │
│ %: 0%                                                                                              │
│ Detalle: Cero tests                                                                                │
│                                                                                                    │
│ ---                                                                                                │
│ Fases                                                                                              │
│                                                                                                    │
│ Fase 1: Fundacion de Dominio                                                                       │
│                                                                                                    │
│ Objetivo: Capa de dominio pura (Java sin Spring) testeable independientemente.                     │
│                                                                                                    │
│ Entregables:                                                                                       │
│ - Value Objects (5): CodigoBarras, Cantidad, Dinero, EmpresaId, ProductoId — records inmutables    │
│ con validacion                                                                                     │
│ - Servicios de dominio (4): CalculadorStock, PoliticaFEFO, ValidadorEstadoTransferencia,           │
│ CalculadorCosto — logica pura sin DB                                                               │
│ - Politicas/Specifications (2): PoliticaDeduccionStock, PoliticaReserva — validan reglas de        │
│ negocio                                                                                            │
│ - Eventos de dominio (8): records inmutables para comunicacion entre aggregates                    │
│ - Constantes (InventarioConstantes): limites de paginacion, prefijos barcode, escalas              │
│ - Registro en Spring: beans de dominio en InventarioConfig                                         │
│ - Tests unitarios: JUnit 5 puro, sin Spring                                                        │
│                                                                                                    │
│ Resultado: mvn test -Dgroups="unit" pasa. Dominio validado.                                        │
│                                                                                                    │
│ ---                                                                                                │
│ Fase 2: Motor de Operaciones (Kardex)                                                              │
│                                                                                                    │
│ Objetivo: El servicio critico que TODAS las operaciones requieren + generacion de barcodes +       │
│ consultas de stock.                                                                                │
│                                                                                                    │
│ Entregables:                                                                                       │
│ - OperacionService: punto unico para crear operaciones en el kardex. Valida stock para operaciones │
│  negativas con PoliticaDeduccionStock                                                              │
│ - BarcodeService: genera codigos de barras unicos con secuencia atomica                            │
│ - StockQueryService: consultas de stock por contenedor, barcode, producto+bodega, consolidado      │
│ paginado                                                                                           │
│ - KardexQueryService: consultas de kardex con filtros y paginacion                                 │
│ - Testcontainers: reemplazar H2 por PostgreSQL en tests                                            │
│ - Tests: unitarios (mocks) + integracion (repos con Testcontainers)                                │
│                                                                                                    │
│ Resultado: Operaciones se crean programaticamente. Stock calculable. Base para todos los flujos.   │
│                                                                                                    │
│ ---                                                                                                │
│ Fase 3: CRUD Catalogos + API Stock/Kardex                                                          │
│                                                                                                    │
│ Objetivo: Primeros endpoints REST funcionales. Quick wins con CRUD simple + exposicion de          │
│ consultas.                                                                                         │
│                                                                                                    │
│ Entregables:                                                                                       │
│ - DTOs request/response para Bodegas, Ubicaciones, Unidades, Conversiones                          │
│ - MapStruct Mappers para catalogos y stock/kardex                                                  │
│ - Servicios CRUD: BodegaService, UbicacionService, UnidadService, ConversionUnidadService          │
│ - Controllers REST (6): Bodegas, Ubicaciones, Unidades, Conversiones, Stock, Kardex — todos con    │
│ Swagger                                                                                            │
│ - Tests: mappers + integracion de controllers                                                      │
│                                                                                                    │
│ Resultado: Swagger UI con endpoints funcionales. Datos de catalogo se crean y consultan via API.   │
│                                                                                                    │
│ ---                                                                                                │
│ Fase 4: Flujo de Recepcion                                                                         │
│                                                                                                    │
│ Objetivo: Primer flujo vertical completo — la operacion que crea stock.                            │
│                                                                                                    │
│ Entregables:                                                                                       │
│ - RecepcionService: crea recepciones con lineas, genera/reutiliza contenedores, crea operaciones   │
│ RECEPCION                                                                                          │
│ - LoteService: busca o crea lotes por empresa+numero                                               │
│ - DTOs: CrearRecepcionRequest con lineas, RecepcionResponse                                        │
│ - Controller: POST /v1/recepciones, GET /v1/recepciones/{id}, listado paginado                     │
│ - Tests: happy path completo (API -> stock consultable -> kardex registrado)                       │
│                                                                                                    │
│ Resultado: Inventario se recibe via API. Contenedores con barcodes generados. Stock real           │
│ consultable.                                                                                       │
│                                                                                                    │
│ ---                                                                                                │
│ Fase 5: Flujo de Ajustes                                                                           │
│                                                                                                    │
│ Objetivo: Ajustes positivos, negativos e informativos. Requerido por Fase 7 (conteo fisico).       │
│                                                                                                    │
│ Entregables:                                                                                       │
│ - AjusteInventarioService: ajustes +/-, cambios de precio, lock pesimista para negativos           │
│ - DTOs: CrearAjusteRequest con lineas, AjusteResponse                                              │
│ - Controller: POST /v1/ajustes, GET /v1/ajustes/{id}, listado paginado                             │
│ - Metodo interno: crearAjusteDesdeConteo() para uso de ConteoFisicoService                         │
│ - Tests: ajuste positivo, negativo con/sin stock, cambio de precio                                 │
│                                                                                                    │
│ Resultado: Stock se ajusta via API. Lock pesimista previene condiciones de carrera.                │
│                                                                                                    │
│ ---                                                                                                │
│ Fase 6: Flujo de Transferencias                                                                    │
│                                                                                                    │
│ Objetivo: Traslados entre bodegas con maquina de estados completa. El flujo mas complejo.          │
│                                                                                                    │
│ Entregables:                                                                                       │
│ - TransferenciaService: ciclo completo BORRADOR -> CONFIRMADO -> DESPACHADO -> EN_TRANSITO ->      │
│ RECIBIDO                                                                                           │
│ - Resolucion FEFO: para tipo POR_PRODUCTO, seleccion automatica de contenedores                    │
│ - Lock pesimista: en despacho, locks ordenados por ID para evitar deadlocks                        │
│ - DTOs: crear, despachar, recibir, respuesta detallada                                             │
│ - Controller: CRUD + PATCH .../confirmar, .../despachar, .../recibir, .../cancelar                 │
│ - Tests: ciclo completo, recepcion parcial, transiciones invalidas, concurrencia                   │
│                                                                                                    │
│ Resultado: Transferencias funcionales con maquina de estados, FEFO y concurrencia segura.          │
│                                                                                                    │
│ ---                                                                                                │
│ Fase 7: Picking + Conteo Fisico + Reservas                                                         │
│                                                                                                    │
│ Objetivo: Los tres flujos restantes que completan la operativa de inventario.                      │
│                                                                                                    │
│ Entregables:                                                                                       │
│ - PickingService: picking con FEFO + lock pesimista + resolucion automatica de contenedores        │
│ - ConteoFisicoService: crear conteo -> registrar cantidades -> aplicar (genera ajustes via Fase 5) │
│ - ReservaService: crear/liberar/cumplir reservas, valida disponibilidad                            │
│ - Controllers (3): ordenes-picking, conteos-fisicos, reservas                                      │
│ - Tests: FEFO ordering, conteo con ajustes, reservas afectan disponibilidad                        │
│                                                                                                    │
│ Resultado: Los 5 flujos principales operan: Recepcion, Ajuste, Transferencia, Picking, Conteo.     │
│                                                                                                    │
│ ---                                                                                                │
│ Fase 8: Extensiones (Merma + MaxMin + Costo)                                                       │
│                                                                                                    │
│ Objetivo: Funcionalidades secundarias que enriquecen el core.                                      │
│                                                                                                    │
│ Entregables:                                                                                       │
│ - MermaService: merma manual y automatica (basada en configuracion)                                │
│ - MaximoMinimoService: CRUD de configs + recalculo de stock + alertas                              │
│ - ValorizacionService: fotos de costo periodicas con calculo de promedio ponderado                 │
│ - Controllers (3): merma, maximos-minimos, costos                                                  │
│ - Tests: merma reduce stock, alertas de minimo, snapshots de costo                                 │
│                                                                                                    │
│ Resultado: Merma, alertas de reposicion y valorizacion de inventario funcionales.                  │
│                                                                                                    │
│ ---                                                                                                │
│ Fase 9: Infraestructura (Eventos, Schedulers, ACL)                                                 │
│                                                                                                    │
│ Objetivo: Capa reactiva y de integracion.                                                          │
│                                                                                                    │
│ Entregables:                                                                                       │
│ - Listeners (3): AuditoriaEventListener (log de auditoria), MaxMinAlertListener (recalculo         │
│ post-evento), ContabilidadIntegrationListener (notificacion a contabilidad)                        │
│ - Scheduled Tasks (4): recalculo nocturno maxmin, alertas vencimiento, foto de costo mensual,      │
│ expiracion de reservas                                                                             │
│ - Adapters ACL (2): ProductoAdapter, ContabilidadAdapter — stubs listos para conectar a            │
│ microservicios externos                                                                            │
│ - Tests: listeners con mocks, verificar que eventos disparan auditoria                             │
│                                                                                                    │
│ Resultado: Sistema reactivo. Eventos se procesan. Tareas programadas corren. Puntos de integracion │
│  definidos.                                                                                        │
│                                                                                                    │
│ ---                                                                                                │
│ Fase 10: Testing Integral + Polish                                                                 │
│                                                                                                    │
│ Objetivo: Cobertura solida y pulido final.                                                         │
│                                                                                                    │
│ Entregables:                                                                                       │
│ - Tests E2E (5): flujo recepcion-a-stock, recepcion-ajuste-transferencia, picking FEFO, conteo     │
│ fisico, concurrencia                                                                               │
│ - Tests unitarios faltantes para todos los servicios                                               │
│ - Tests de integracion para controllers y queries nativos                                          │
│ - Infraestructura de test: base class Testcontainers, test data builders, tags JUnit 5             │
│ - Polish: Swagger completo, limites de paginacion, readOnly en queries, revision de WHERE activo = │
│  true                                                                                              │
│                                                                                                    │
│ Resultado: mvn verify pasa limpio. Suite completa: unit, integration, e2e.                         │
│                                                                                                    │
│ ---                                                                                                │
│ Grafo de Dependencias                                                                              │
│                                                                                                    │
│ Fase 1 (Dominio)                                                                                   │
│   |                                                                                                │
│   v                                                                                                │
│ Fase 2 (Motor Operaciones) ---> Fase 3 (Catalogos + Stock API)                                     │
│   |                                                                                                │
│   v                                                                                                │
│ Fase 4 (Recepcion) ---> Fase 5 (Ajustes) ---> Fase 7 (Picking + Conteo + Reservas)                 │
│   |                         |                                                                      │
│   v                         v                                                                      │
│ Fase 6 (Transferencias)   Fase 8 (Extensiones)                                                     │
│   |                         |                                                                      │
│   +-------+---------+-------+                                                                      │
│           |                                                                                        │
│           v                                                                                        │
│   Fase 9 (Infraestructura)                                                                         │
│           |                                                                                        │
│           v                                                                                        │
│   Fase 10 (Testing + Polish)                                                                       │
│                                                                                                    │
│ Archivos clave de referencia                                                                       │
│                                                                                                    │
│ Archivo: .github/AGENTS.md                                                                         │
│ Uso: Reglas de arquitectura, naming, concurrencia, anti-patrones                                   │
│ ────────────────────────────────────────                                                           │
│ Archivo: docs/inventario_arquitectura_java.md                                                      │
│ Uso: Codigo de referencia para servicios y entidades                                               │
│ ────────────────────────────────────────                                                           │
│ Archivo: db/inventario_ddl_postgresql_es.sql                                                       │
│ Uso: DDL fuente de verdad                                                                          │
│ ────────────────────────────────────────                                                           │
│ Archivo: repositorio/contenedor/OperacionRepository.java                                           │
│ Uso: Queries de stock, FEFO, kardex                                                                │
│ ────────────────────────────────────────                                                           │
│ Archivo: domain/modelo/contenedor/Contenedor.java                                                  │
│ Uso: Aggregate root central                                                             