# Exodia Inventario

Microservicio de inventario del ERP Exodia construido con Java 21 y Spring Boot. El diseño del dominio está centrado en contenedores trazables por código de barras y en un kardex de operaciones donde el stock siempre se calcula; no se persiste como una columna acumulada.

El repositorio ya no está solo en fase de base técnica. Hoy combina:

- modelo JPA amplio
- migraciones Flyway
- servicios de dominio
- servicios de aplicación
- API REST documentada con Swagger/OpenAPI
- DTOs y mappers
- una primera suite de pruebas unitarias

## Panorama actual

Estado observado directamente en el código:

- 30 entidades JPA en `domain/`
- 20 migraciones Flyway en `src/main/resources/db/migration`
- 18 servicios de aplicación entre escritura y consulta
- 15 controllers REST
- 15 mappers MapStruct
- 15 archivos de test unitario

Cobertura funcional expuesta hoy por API:

- catálogos: bodegas, ubicaciones, unidades, conversiones
- consultas: stock y kardex
- recepciones
- ajustes
- transferencias
- picking
- conteos físicos
- reservas
- extensiones: máximos y mínimos, merma, valorización y fotos de costo

Pendientes o incompletos a nivel de plataforma:

- no hay CRUD/API para empresas
- no hay seguridad JWT todavía
- la auditoría usa usuario fijo
- no hay tests de integración ni end-to-end
- no hay `application-prod.yml`

## Stack y versiones

| Componente | Versión / estado |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.0.4 |
| Maven Wrapper | 3.9.14 |
| Maven Wrapper plugin | 3.3.4 |
| Spring Data JPA | gestionado por Spring Boot 4.0.4 |
| Flyway | gestionado por Spring Boot 4.0.4 |
| PostgreSQL | base principal; la documentación interna apunta a PostgreSQL 18 |
| Spring Security | gestionado por Spring Boot 4.0.4 |
| SpringDoc OpenAPI | 3.0.0 |
| MapStruct | 1.6.3 |
| Lombok | 1.18.44 |
| H2 | dependencia de test declarada en `pom.xml` |

Coordenadas Maven actuales:

- `groupId`: `com.Exodia`
- `artifactId`: `Exodia-inventory`
- `version`: `0.0.1-SNAPSHOT`

## Principios del dominio

- el stock nunca se persiste; se reconstruye con `SUM(cantidad)` sobre `inv_operaciones`
- el contenedor es la unidad mínima trazable del inventario
- toda entrada, salida o ajuste relevante debe pasar por el kardex
- el proyecto usa DDD pragmático con capas claras de dominio, aplicación, persistencia e interfaz
- la lectura y la escritura ya están separadas en `aplicacion/consulta` y `aplicacion/comando`

## Arquitectura del código

Paquete base: `com.exodia.inventario`

Estructura principal:

- `config/`: configuración Spring, OpenAPI, seguridad y auditoría JPA
- `domain/`: entidades JPA, enums, eventos, value objects, políticas y servicios de dominio
- `aplicacion/comando/`: servicios de escritura
- `aplicacion/consulta/`: servicios de lectura
- `repositorio/`: repositorios Spring Data JPA y proyecciones
- `interfaz/rest/`: controllers REST
- `interfaz/dto/`: request y response DTOs
- `interfaz/mapeador/`: mappers MapStruct
- `excepcion/`: excepciones de negocio y manejador global
- `util/`: constantes del módulo

Patrones visibles en el código:

- `ApiResponse<T>` como contrato de respuesta estándar
- `PaginaResponse<T>` para respuestas paginadas
- consultas de stock y kardex combinando JPQL y SQL nativo
- `open-in-view: false`
- `ddl-auto: validate`
- auditoría JPA habilitada vía `@EnableJpaAuditing`

## Base de datos

La base ejecutable del esquema está en las migraciones Flyway. Los documentos del directorio `db/` sirven como referencia de modelado y nomenclatura.

Grupos de tablas presentes:

- catálogos: empresas, bodegas, ubicaciones, unidades, conversiones, tipos y estados
- núcleo: lotes, contenedores, operaciones y reservas
- flujos: recepciones, transferencias, ajustes, picking y conteos
- extensiones: máximos/mínimos, merma, fotos de costo, secuencias de barcode y auditoría

Datos iniciales cargados por migración:

- tipos de operación
- tipos de ajuste
- estados de contenedor
- estados de transferencia

Importante:

- no existe CRUD de empresas en la API actual
- para usar casi todos los endpoints debes contar con al menos un registro en `inv_empresas`

## Documentos base del repositorio

- [`CLAUDE.md`](CLAUDE.md): arranque rápido y reglas mínimas
- [`.github/AGENTS.md`](.github/AGENTS.md): decisiones arquitectónicas y estándares del proyecto
- [`docs/inventario_arquitectura_java.md`](docs/inventario_arquitectura_java.md): arquitectura objetivo y estructura de referencia
- [`db/inventario_modelo_db.md`](db/inventario_modelo_db.md): modelo de base de datos y decisiones de diseño
- [`db/inventario_mapeo_bd_java.md`](db/inventario_mapeo_bd_java.md): mapeo entre nombres de BD y clases Java
- [`db/inventario_ddl_postgresql_es.sql`](db/inventario_ddl_postgresql_es.sql): DDL de referencia en PostgreSQL
- [`baseDeERPdeMigracion.md`](baseDeERPdeMigracion.md): análisis técnico del inventario legado a replicar o mejorar
- [`plan.md`](plan.md): roadmap por fases

## Prerrequisitos

- JDK 21 instalado
- `JAVA_HOME` configurado correctamente
- PostgreSQL disponible localmente
- base de datos de desarrollo creada: `exodia_inventario`
- opcionalmente base de datos de prueba: `exodia_inventario_test`

Configuración por defecto en `application.yml`:

- URL: `jdbc:postgresql://localhost:5432/exodia_inventario`
- usuario: `exodia`
- contraseña: `exodia`
- puerto HTTP: `8080`
- context path: `/api`

Variables de entorno soportadas:

- `DB_USERNAME`
- `DB_PASSWORD`

Perfiles disponibles:

- `default`: PostgreSQL local en `exodia_inventario`
- `dev`: habilita `show-sql`, aumenta logs del módulo y deja `flyway.clean-disabled: false`
- `test`: usa `exodia_inventario_test` en PostgreSQL local

## Puesta en marcha local

1. Configura Java 21 y `JAVA_HOME`.
2. Crea la base de datos en PostgreSQL.
3. Exporta credenciales si no usarás las de ejemplo.
4. Ejecuta el proyecto con Maven Wrapper.

Ejemplo:

```bash
export JAVA_HOME=/ruta/a/jdk-21
export DB_USERNAME=exodia
export DB_PASSWORD=exodia
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Comandos útiles:

```bash
./mvnw clean compile
./mvnw test
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Empresa base para desarrollo

La API actual depende del header `X-Empresa-Id`, pero no expone endpoints para administrar empresas. Después de que Flyway cree el esquema, puedes insertar una empresa base manualmente:

```sql
INSERT INTO inv_empresas (codigo, nombre, nit)
VALUES ('EXODIA', 'Exodia', NULL);
```

Si es el primer registro, normalmente quedará con `id = 1`, por lo que podrás usar:

```http
X-Empresa-Id: 1
```

## API disponible hoy

Casi toda la API es multiempresa y espera el header `X-Empresa-Id`.

Nota importante sobre rutas:

- el proyecto tiene `server.servlet.context-path=/api`
- los controllers están mapeados con `/api/v1/...`
- por eso las rutas efectivas hoy quedan como `/api/api/v1/...`

Módulos expuestos:

- `/api/api/v1/bodegas`: `POST`, `GET/{id}`, `GET`, `PATCH/{id}`, `DELETE/{id}/desactivar`
- `/api/api/v1/ubicaciones`: `POST`, `GET/{id}`, `GET?bodegaId=...`, `PATCH/{id}`, `DELETE/{id}/desactivar`
- `/api/api/v1/unidades`: `POST`, `GET/{id}`, `GET`, `PATCH/{id}`, `DELETE/{id}/desactivar`
- `/api/api/v1/conversiones-unidad`: `POST`, `GET/{id}`, `GET`, `DELETE/{id}/desactivar`
- `/api/api/v1/inventario/stock`: `GET /contenedor/{id}`, `GET /barcode/{codigoBarras}`, `GET /producto-bodega`, `GET /consolidado`, `GET /agrupado`, `GET /disponible-fefo`
- `/api/api/v1/inventario/kardex`: `GET`
- `/api/api/v1/recepciones`: `POST`, `GET/{id}`, `GET`
- `/api/api/v1/ajustes`: `POST`, `GET/{id}`, `GET`
- `/api/api/v1/transferencias`: `POST`, `GET/{id}`, `GET`, `PATCH /{id}/confirmar`, `PATCH /{id}/despachar`, `PATCH /{id}/recibir`, `PATCH /{id}/cancelar`
- `/api/api/v1/picking`: `POST`, `PATCH /{id}/ejecutar`, `GET/{id}`, `GET`, `PATCH /{id}/cancelar`
- `/api/api/v1/conteos`: `POST`, `POST /{id}/lineas`, `PATCH /{id}/aplicar`, `GET/{id}`, `GET`, `PATCH /{id}/cancelar`
- `/api/api/v1/reservas`: `POST`, `GET/{id}`, `GET /contenedor/{contenedorId}`, `PATCH /{id}/cancelar`
- `/api/api/v1/maximos-minimos`: `POST`, `GET/{id}`, `GET?bodegaId=...`, `PATCH/{id}`, `DELETE/{id}/desactivar`
- `/api/api/v1/mermas`: `POST`
- `/api/api/v1/valorizacion`: `POST /foto-costo`, `GET /fotos-costo`

## Swagger y OpenAPI

- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api/api-docs`

## Ejemplos rápidos

Crear una bodega:

```bash
curl --request POST 'http://localhost:8080/api/api/v1/bodegas' \
  --header 'Content-Type: application/json' \
  --header 'X-Empresa-Id: 1' \
  --data '{
    "codigo": "BOD-CEN",
    "nombre": "Bodega Central",
    "direccion": "San Salvador",
    "ciudad": "San Salvador",
    "pais": "El Salvador",
    "esProductoTerminado": false,
    "esConsignacion": false
  }'
```

Crear una recepción:

```bash
curl --request POST 'http://localhost:8080/api/api/v1/recepciones' \
  --header 'Content-Type: application/json' \
  --header 'X-Empresa-Id: 1' \
  --data '{
    "bodegaId": 1,
    "tipoRecepcion": "MANUAL",
    "proveedorId": 10,
    "comentarios": "Carga inicial",
    "lineas": [
      {
        "productoId": 1001,
        "unidadId": 1,
        "ubicacionId": 1,
        "cantidad": 12,
        "precioUnitario": 8.50,
        "numeroLote": "L-20260325",
        "fechaVencimiento": "2026-12-31"
      }
    ]
  }'
```

Consultar stock consolidado:

```bash
curl 'http://localhost:8080/api/api/v1/inventario/stock/consolidado?pagina=0&tamanio=20' \
  --header 'X-Empresa-Id: 1'
```

## Pruebas

Hoy el repositorio contiene únicamente pruebas unitarias. No se encontraron `@SpringBootTest`, `@Testcontainers` ni suites de integración o e2e.

Estado actual del testing:

- 15 archivos de test unitario
- 0 tests de integración
- 0 tests end-to-end

Cobertura observada:

- value objects
- políticas de dominio
- servicios de dominio
- `BarcodeService`
- `OperacionService`
- consultas de stock y kardex

Aunque la superficie REST ya es mucho mayor, todavía no hay cobertura automática de controllers ni de flujos completos.

## Limitaciones y decisiones actuales

- seguridad abierta para desarrollo: `SecurityConfig` permite todas las solicitudes
- auditoría técnica provisional: `JpaAuditConfig` devuelve siempre el usuario `1L`
- no hay CRUD/API para empresas
- el proyecto requiere `JAVA_HOME`; sin eso `./mvnw` no arranca
- el perfil `test` apunta a PostgreSQL local aunque `pom.xml` también declara `H2`
- valorización expone fotos de costo por API, pero el cálculo actual usa `costoUnitario = 0`, así que no es un costeo final de negocio todavía
- la API ya cubre muchos flujos, pero la cobertura de pruebas sigue concentrada en piezas base

## Siguiente enfoque recomendado

El siguiente paso natural no es abrir más endpoints, sino endurecer lo ya construido:

- integrar seguridad JWT real
- conectar auditoría con el usuario autenticado
- agregar tests de integración para flujos verticales
- revisar valorización para sustituir el costo placeholder por una lógica real de costeo
