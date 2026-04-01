# QA de Cierre — Inventario

## Objetivo

Validar que el microservicio ya cubre el inventario propio heredado del ERP anterior y que los flujos internos principales funcionan de extremo a extremo.

Este cierre **no** incluye:

- integraciones reales con compras, ventas, produccion y contabilidad;
- inventario documental;
- spool de impresion o PDF pesado.

## Precondiciones

- Java 21 y `JAVA_HOME` configurado.
- Base PostgreSQL disponible.
- Migraciones Flyway aplicadas.
- Al menos una empresa en `inv_empresas`.
- Producto maestro accesible o, en ambiente local, adapters stub habilitados.

Header base para casi todas las pruebas:

```http
X-Empresa-Id: 1
X-Correlation-Id: qa-cierre-001
```

## Criterio de salida

Se considera aprobado si:

- no hay errores de compilacion;
- migraciones aplican limpias;
- smoke test completo pasa;
- casos negativos responden con error de negocio esperado;
- kardex, stock, auxiliar y valorizacion reflejan los movimientos ejecutados;
- etiquetas de contenedor y ubicacion se generan;
- en `prod`, `X-Empresa-Id` coincide o se resuelve desde el JWT.

## Paso 1. Validacion tecnica minima

Ejecutar:

```bash
./mvnw clean compile
./mvnw test
```

Validar:

- sin errores de compilacion;
- migraciones hasta `V412__agregar_motivo_merma.sql`;
- tests unitarios e integracion en verde.

## Paso 2. Smoke test funcional

Orden recomendado:

1. catalogos base
2. recepcion
3. consultas de stock y kardex
4. conversion
5. movimientos y standby
6. reservas
7. picking
8. transferencias
9. ajustes
10. conteo fisico
11. mermas
12. venta facturada
13. maximos/minimos
14. valorizacion y reportes
15. etiquetas
16. seguridad y trazabilidad

## Paso 3. Matriz por flujo

### 3.1 Catalogos base

Endpoints:

- `POST /api/v1/bodegas`
- `POST /api/v1/ubicaciones`
- `POST /api/v1/unidades`
- `POST /api/v1/conversiones-unidad`
- `PATCH /api/v1/configuracion-empresa`
- `POST /api/v1/configuracion-producto`

Validar:

- se crea una bodega con ubicacion standby configurada;
- la ubicacion queda asociada a la bodega correcta;
- la unidad puede reutilizarse en recepcion y conversion;
- la conversion queda disponible para `inventario/conversiones`;
- configuracion empresa guarda politica, alertas y prefijo barcode;
- configuracion producto guarda lote, vencimiento y tolerancia merma.

### 3.2 Recepcion manual

Endpoint:

- `POST /api/v1/recepciones`

Caso positivo:

- recibir producto con `cantidad`, `precioUnitario`, `unidadId`, `ubicacionId`;
- repetir con `cantidadMerma` para validar merma automatica;
- opcionalmente incluir `numeroLote` y `fechaVencimiento`.

Validar:

- se crea `numeroRecepcion`;
- se genera o reutiliza barcode segun reglas;
- se crea contenedor;
- se registra operacion positiva;
- si hay `cantidadMerma`, se registra merma automatica con motivo `RECEPCION`;
- stock final del contenedor es `cantidad - cantidadMerma`.

Casos negativos:

- `cantidadMerma > cantidad`;
- producto inexistente;
- unidad o ubicacion fuera de la empresa;
- barcode manual duplicado cuando no aplica reutilizacion.

### 3.3 Stock y kardex

Endpoints:

- `GET /api/v1/inventario/stock/contenedor/{id}`
- `GET /api/v1/inventario/stock/barcode/{codigo}`
- `GET /api/v1/inventario/stock/producto-bodega`
- `GET /api/v1/inventario/stock/consolidado`
- `GET /api/v1/inventario/stock/agrupado`
- `GET /api/v1/inventario/kardex`

Validar:

- stock por contenedor coincide con recepcion y ajustes hechos;
- stock por barcode coincide con contenedor;
- agrupado producto-bodega suma correctamente;
- kardex muestra entradas y salidas en orden cronologico;
- consolidado refleja cantidad reservada y disponible.

### 3.4 Conversion de inventario

Endpoint:

- `POST /api/v1/inventario/conversiones`

Caso positivo:

- convertir total o parcialmente un contenedor usando una conversion configurada.

Validar:

- en conversion parcial se genera contenedor nuevo;
- ambos contenedores quedan con cantidades consistentes;
- se registran operaciones de salida/entrada correspondientes;
- barcode destino nuevo cuando corresponde.

Casos negativos:

- conversion sin regla;
- cantidad mayor al disponible;
- contenedor con reservas activas.

### 3.5 Movimientos internos y standby

Endpoints:

- `POST /api/v1/movimientos/contenedores/{id}/mover`
- `POST /api/v1/movimientos/contenedores/{id}/enviar-standby`
- `POST /api/v1/movimientos/contenedores/{id}/sacar-standby`

Validar:

- el contenedor cambia de ubicacion;
- enviar a standby solo funciona con estado `DISPONIBLE`;
- mover no desbloquea estados especiales por accidente;
- no permite mover contenedores con reservas activas;
- sacar de standby exige misma bodega y ubicacion operativa.

### 3.6 Reservas

Endpoints:

- `POST /api/v1/reservas`
- `GET /api/v1/reservas/{id}`
- `GET /api/v1/reservas/contenedor/{contenedorId}`
- `PATCH /api/v1/reservas/{id}/cancelar`

Validar:

- la reserva reduce `cantidadDisponible` sin cambiar stock total;
- cancelar libera disponibilidad;
- no permite reservar mas que el disponible.

### 3.7 Picking

Endpoints:

- `POST /api/v1/picking`
- `PATCH /api/v1/picking/{id}/ejecutar`
- `GET /api/v1/picking/{id}`
- `PATCH /api/v1/picking/{id}/cancelar`

Validar:

- crea orden y lineas;
- al ejecutar, descuenta stock real;
- respeta politica FEFO/FIFO/MANUAL configurada;
- publica evento de picking;
- maximos/minimos se recalculan despues del descuento.

Casos negativos:

- stock insuficiente;
- orden ya cancelada;
- contenedor no permitido por estado.

### 3.8 Transferencias

Endpoints:

- `POST /api/v1/transferencias`
- `PATCH /api/v1/transferencias/{id}/confirmar`
- `PATCH /api/v1/transferencias/{id}/despachar`
- `PATCH /api/v1/transferencias/{id}/recibir`
- `PATCH /api/v1/transferencias/{id}/cancelar`

Validar:

- flujo de estados consistente;
- despacho descuenta en origen;
- recepcion acredita en destino;
- recepcion parcial y completa funcionan;
- eventos de despacho/recepcion recalculan max/min.

### 3.9 Ajustes

Endpoints:

- `POST /api/v1/ajustes`
- `GET /api/v1/ajustes/{id}`
- `GET /api/v1/ajustes`

Validar:

- ajuste de cantidad crea movimiento trazable;
- ajuste de precio no rompe stock;
- motivo queda guardado;
- kardex y auxiliar reflejan el cambio.

### 3.10 Conteo fisico

Endpoints:

- `POST /api/v1/conteos`
- `POST /api/v1/conteos/{id}/lineas`
- `PATCH /api/v1/conteos/{id}/aplicar`
- `PATCH /api/v1/conteos/{id}/cancelar`

Validar:

- permite registrar conteo;
- aplicar genera ajustes por diferencia;
- si no hay diferencia, no debe crear ruido innecesario;
- queda trazabilidad completa del conteo y ajuste generado.

### 3.11 Merma manual

Endpoints:

- `POST /api/v1/mermas`
- `GET /api/v1/mermas/{id}`
- `GET /api/v1/mermas`
- `POST /api/v1/config-merma`
- `PATCH /api/v1/config-merma/{id}`

Validar:

- merma manual exige cantidad positiva;
- guarda `tipoMerma` y `motivoCodigo`;
- crea operacion de merma;
- respeta configuracion por empresa/producto/bodega;
- respeta tolerancia de producto;
- la frecuencia en dias bloquea nuevas mermas cuando aplica.

Casos negativos:

- merma mayor al stock disponible;
- merma mayor al porcentaje/cantidad fija configurada;
- motivo nulo en flujo manual si el negocio decide volverlo obligatorio en frontend.

### 3.12 Ajuste por venta facturada

Endpoint:

- `POST /api/v1/ventas-ajustes`

Validar:

- descuenta inventario segun politica configurada;
- genera ajuste y operaciones trazables;
- no publica duplicado tecnico de salida por contenedor;
- publica evento agregado de venta ajustada;
- stock, kardex y auxiliar quedan consistentes.

### 3.13 Maximos y minimos

Endpoints:

- `POST /api/v1/maximos-minimos`
- `PATCH /api/v1/maximos-minimos/{id}`
- `GET /api/v1/maximos-minimos`

Validar:

- stock actual calculado se actualiza despues de recepcion, picking, venta, merma y transferencias;
- si cae bajo minimo, se publica alerta.

### 3.14 Valorizacion y reportes

Endpoints:

- `POST /api/v1/valorizacion/foto-costo`
- `GET /api/v1/valorizacion/fotos-costo`
- `GET /api/v1/reportes/auxiliar-inventario`
- `GET /api/v1/reportes/auxiliar-inventario/exportar-csv`
- `GET /api/v1/reportes/valorizacion-actual`
- `GET /api/v1/reportes/valorizacion-actual/exportar-csv`

Validar:

- auxiliar reconstruye saldo inicial, entradas, salidas y saldo final;
- valorizacion actual coincide con stock agrupado y costo promedio ponderado;
- CSV exporta encabezados y filas completas;
- valorizacion y auxiliar reflejan ajuste, merma, transferencia, picking y venta.

### 3.15 Etiquetas

Endpoints:

- `GET /api/v1/etiquetas/contenedores/{id}`
- `GET /api/v1/etiquetas/contenedores/{id}/zpl`
- `GET /api/v1/etiquetas/contenedores/{id}/svg`
- `GET /api/v1/etiquetas/ubicaciones/{id}`
- `GET /api/v1/etiquetas/ubicaciones/{id}/zpl`
- `GET /api/v1/etiquetas/ubicaciones/{id}/svg`

Validar:

- la etiqueta de contenedor contiene barcode, producto, bodega, ubicacion, unidad y stock;
- la etiqueta de ubicacion contiene codigo, nombre y tipo;
- si ubicacion no tiene barcode propio, usa `codigo`;
- `zpl` y `svg` se devuelven sin caracteres rotos.

### 3.16 Seguridad y trazabilidad

Validar en `prod`:

- con JWT que trae `empresa_id` o `tenant_id` y sin header manual, el request sigue funcionando;
- con header distinto al claim, responde `403`;
- `X-Correlation-Id` se refleja en la respuesta;
- auditoria usa identidad real cuando JWT trae `user_id`, `uid` o `sub`.

## Paso 4. Casos de regresion clave

Ejecutar despues del smoke:

- recepcion con merma automatica y luego consulta de stock;
- venta ajustada y luego auxiliar de inventario;
- transferencia despachada/recibida y luego max/min;
- contenedor reservado intentando movimiento y conversion;
- salida a standby y retorno sin cambiar indebidamente el estado del contenedor.

## Paso 5. Evidencia a guardar

Guardar:

- salida de `./mvnw clean compile`;
- salida resumida de `./mvnw test`;
- capturas o payloads de `stock`, `kardex`, `auxiliar`, `valorizacion` y `etiquetas`;
- evidencia de un `403` por mismatch de tenant;
- evidencia de un `X-Correlation-Id` reutilizado.

## Estado esperado hoy

Si esta guia pasa completa, el inventario interno del ERP anterior puede considerarse cubierto en un nivel alto.

Lo que seguiria pendiente ya no seria "inventario propio", sino integraciones reales o satelites:

- compras,
- ventas,
- produccion,
- contabilidad,
- inventario documental,
- impresion pesada tipo PDF/spool.
