# Brechas de Paridad con el ERP Original e Integracion Propuesta

## 1. Objetivo

Este documento resume que parte del inventario del ERP original ya quedo absorbida por este backend, que parte sigue faltando para llegar a una paridad mas alta y como conviene integrar esos faltantes sin convertir este microservicio en un nuevo monolito.

La idea no es "meter todo el ERP aqui". La idea correcta es:

- mantener el nucleo de inventario dentro de este servicio,
- integrar los modulos externos que son de otro bounded context,
- y solo traer al inventario lo que realmente le pertenece.

---

## 2. Resumen ejecutivo

El backend actual ya cubre el nucleo fuerte del inventario:

- contenedor trazable por barcode,
- kardex por operaciones,
- recepciones,
- consultas de stock y kardex,
- ajustes,
- transferencias,
- picking,
- reservas,
- conteos fisicos,
- maximos y minimos,
- merma manual,
- valorizacion.

Lo que no esta a paridad con el ERP original no es tanto "mas inventario core", sino integraciones y modulos satelite:

- catalogo maestro de productos,
- compras y recepciones contra orden de compra,
- ventas, despacho o facturacion para cierre de stock,
- contabilidad,
- impresion de etiquetas y reportes PDF,
- inventario documental,
- configuracion real de merma automatica,
- hardening de plataforma para operar en produccion.

Conclusion practica:

- este proyecto ya es un microservicio de inventario serio,
- pero todavia no es la replica total del inventario dentro del ERP original,
- ni debe intentar serlo mezclando dominios que deberian seguir separados.

---

## 3. Matriz rapida de brechas

| Area | Estado en este backend | Diagnostico | Recomendacion |
| --- | --- | --- | --- |
| Nucleo de inventario | Alto | Ya esta bastante cubierto | Mantener aqui |
| Catalogos operativos propios | Alto | Bodegas, ubicaciones, unidades, conversiones y max/min ya tienen API | Mantener aqui |
| Productos maestro | Parcial | Solo se referencia `productoId`; no hay modulo propio de productos | Integrar, no duplicar |
| Empresas | Parcial | Existe entidad, pero no API de administracion | Resolver via servicio maestro o bootstrap externo |
| Compras / ordenes de compra | Parcial | La referencia existe, pero no la integracion funcional | Integrar |
| Ventas / facturacion / despacho | Bajo | No existe cierre real de inventario contra venta | Integrar con alta prioridad |
| Requisiciones / produccion | Parcial | Hay picking y `TipoReferencia`, pero no integracion real | Integrar segun negocio |
| Contabilidad | Parcial | Existe puerto ACL, pero solo stub | Integrar async |
| Merma configurable / automatica | Parcial | Existe modelo, pero no esta conectado al flujo | Cerrar dentro de inventario |
| Etiquetas / PDF | Bajo | No hay modulo de impresion ni reportes de etiquetas | Integrar o separar en servicio de reportes |
| Inventario documental | Bajo | No existe modulo equivalente | Separar como bounded context distinto |
| Seguridad / tenant / auditoria prod | Bajo | Sigue siendo brecha de plataforma | Cerrar antes de produccion seria |
| Outbox / idempotencia / observabilidad | Bajo | Todavia faltan piezas de integracion robusta | Cerrar como backlog tecnico |

---

## 4. Que del ERP original si quedo reflejado aqui

Tomando como base `baseDeERPdeMigracion.md`, este backend ya absorbio la mayor parte del inventario operativo:

- modelo por contenedor trazable,
- libro mayor de operaciones,
- recepcion manual y estructurada,
- consultas consolidadas de inventario,
- stock por producto y bodega,
- kardex,
- ajustes de cantidad y precio,
- transferencias por producto y por contenedor,
- picking,
- reservas,
- conteos fisicos,
- maximos y minimos,
- merma manual,
- fotos de costo.

Eso significa que el proyecto ya no esta en una fase de "maqueta". El core de inventario ya fue extraido del ERP y rehecho como servicio.

---

## 5. Brechas funcionales principales y como integrarlas

## 5.1. Catalogo maestro de productos

### Estado actual

El backend guarda `productoId` en contenedores, operaciones, picking, reservas, recepciones y transferencias, pero no administra productos como catalogo propio. Eso es consistente con un inventario desacoplado.

Ademas, ya existe un puerto ACL para esta integracion:

- `src/main/java/com/exodia/inventario/infraestructura/integracion/ProductoAdapter.java`
- `src/main/java/com/exodia/inventario/infraestructura/integracion/ProductoAdapterStub.java`

### Lo que falta

- validar que el producto exista en el maestro real,
- obtener datos minimos del producto,
- opcionalmente consultar reglas por producto:
  - unidad base,
  - si maneja lote,
  - si maneja vencimiento,
  - si permite fraccionamiento,
  - dias de alerta,
  - configuracion de merma esperada.

### Recomendacion

No crear un CRUD completo de productos dentro de inventario. Mejor:

- mantener `productoId` como referencia externa,
- integrar via REST o gRPC con el servicio maestro de productos,
- guardar en inventario solo la data estrictamente necesaria para trazabilidad historica.

### Contrato minimo sugerido

- `productoId`
- `codigo`
- `nombre`
- `unidadBaseId` o `unidadBaseCodigo`
- `manejaLote`
- `manejaVencimiento`
- `activo`

---

## 5.2. Compras y recepciones contra orden de compra

### Estado actual

El modelo ya esta preparado para referencias externas. `Operacion` tiene:

- `tipoReferencia`
- `referenciaId`
- `referenciaLineaId`

Y `TipoReferencia` ya contempla `ORDEN_COMPRA`.

### Lo que falta

No existe integracion funcional con compras para:

- consultar lineas pendientes de recibir,
- validar cantidades pendientes,
- registrar recepcion ligada a una orden real,
- notificar a compras lo ya recepcionado.

### Recomendacion

No meter logica de compras dentro de inventario. Mejor:

1. `Compras` sigue siendo dueno de la orden de compra.
2. `Inventario` recibe una instruccion para recepcionar contra una referencia externa.
3. `Inventario` confirma recepcion y emite respuesta o evento con cantidades efectivamente recibidas.
4. `Compras` actualiza su estado interno.

### Integracion sugerida

Sincrona:

- `ComprasAdapter.obtenerDetalleOrdenCompra(empresaId, ordenCompraId, lineaId)`
- `ComprasAdapter.validarPendienteRecepcion(...)`

Asincrona:

- evento `inventario.recepcion.confirmada`

### Datos minimos

- `ordenCompraId`
- `ordenCompraLineaId`
- `productoId`
- `unidadId`
- `cantidadEsperada`
- `cantidadRecibida`
- `proveedorId`
- `precioUnitario`

---

## 5.3. Ventas, despacho y facturacion

### Estado actual

El backend ya tiene:

- reservas,
- picking,
- operaciones de salida,
- `TipoReferencia.VENTA`.

Lo que no tiene es la integracion que cierre inventario contra el flujo comercial.

### Lo que falta

- reservar stock para pedido o venta,
- pickear contra una orden comercial real,
- confirmar la salida definitiva por despacho o entrega,
- reconciliar lo vendido con lo descontado.

### Decision critica

Antes de integrar, debes definir cual es el momento real de descuento de stock:

- al reservar,
- al pickear,
- al despachar,
- al facturar.

### Recomendacion

Para un inventario serio de venta, la opcion mas sana suele ser:

- `reserva` cuando el pedido se confirma,
- `picking` cuando se prepara,
- `salida definitiva` cuando el despacho o entrega se confirma,
- `factura` solo referencia comercial y contable, no el punto tecnico primario de deduccion.

Facturar no siempre significa que el producto ya salio fisicamente. Por eso no recomiendo usar la factura como gatillo unico de descuento si el proceso logistica y comercialmente estan separados.

### Integracion sugerida

Sincrona:

- `VentasAdapter.obtenerLineaPedido(...)`
- `VentasAdapter.validarEstadoDespachable(...)`

Asincrona:

- evento `inventario.reserva.creada`
- evento `inventario.picking.completado`
- evento `inventario.salida.confirmada`

### Prioridad

Esta es la brecha funcional mas importante si el objetivo del sistema es inventario para venta.

---

## 5.4. Requisiciones internas y produccion

### Estado actual

El backend ya tiene picking y referencias genericas. `TipoReferencia` incluso contempla `ORDEN_PRODUCCION`.

### Lo que falta

- integracion real con requisiciones internas,
- integracion real con ordenes de produccion,
- consumo estructurado de materiales,
- devolucion o sobrante de materiales al inventario.

### Recomendacion

Si vas a tener manufactura o consumos internos, no metas ese workflow completo dentro de inventario. Mejor:

- `Produccion` o `Abastecimiento` generan la requisicion,
- `Inventario` reserva, pickea y descuenta,
- `Produccion` consume o devuelve,
- `Inventario` registra el movimiento final.

---

## 5.5. Contabilidad

### Estado actual

Ya existe el puerto ACL:

- `src/main/java/com/exodia/inventario/infraestructura/integracion/ContabilidadAdapter.java`
- `src/main/java/com/exodia/inventario/infraestructura/integracion/ContabilidadAdapterStub.java`

Eso es bueno: el proyecto ya reconoce que contabilidad es otro bounded context.

### Lo que falta

- implementacion real del adapter,
- definicion de que movimientos de inventario generan asiento,
- valorizacion monetaria consistente para esos asientos,
- manejo de reintentos e idempotencia.

### Recomendacion

No hagas que inventario escriba en tablas contables. Mejor:

- inventario publica el hecho de negocio,
- contabilidad decide el asiento,
- si hace falta, inventario envia monto y metadatos minimos.

### Mejor patron

Integracion asincrona con outbox:

- `inventario.stock.ajustado`
- `inventario.transferencia.despachada`
- `inventario.transferencia.recibida`
- `inventario.recepcion.confirmada`
- `inventario.merma.registrada`

---

## 5.6. Merma configurable y merma automatica

### Estado actual

El modelo ya existe:

- `ConfigMerma`
- `RegistroMerma`

Pero el flujo actual de merma solo registra merma manual. La configuracion de merma no esta expuesta ni aplicada.

### Lo que falta

- CRUD de configuracion de merma,
- reglas por empresa, producto y bodega,
- motivos de merma,
- procesamiento automatico si el negocio realmente lo necesita.

### Recomendacion

Esto si pertenece al dominio de inventario y conviene cerrarlo aqui, no externalizarlo.

### Alcance sugerido

Fase 1:

- CRUD de `ConfigMerma`
- motivos de merma
- aplicacion manual con tipo parametrizado

Fase 2:

- merma automatica solo si hay un caso de negocio claro
- scheduler o trigger de negocio explicito

No recomiendo automatizar merma "por defecto" solo porque el ERP original tenia la idea. Si no hay una regla operativa muy clara, solo agregas ruido y riesgo.

---

## 5.7. Etiquetas, PDF y operacion con scanner

### Estado actual

El sistema ya genera y usa barcodes, pero no ofrece:

- impresion de etiquetas,
- PDF de barcodes,
- reimpresion,
- diseno de plantillas,
- servicio de spool o colas de impresion.

### Recomendacion

Mantener la generacion y unicidad del barcode en inventario, pero separar la impresion.

Opciones:

- modulo de reportes,
- BFF del ERP,
- microservicio de etiquetas.

### Integracion sugerida

Inventario expone:

- datos del barcode,
- metadata del contenedor,
- ubicacion,
- lote,
- fecha vencimiento.

El servicio de etiquetas convierte eso en:

- PDF,
- ZPL,
- plantilla de impresion.

---

## 5.8. Inventario documental

### Estado actual

No existe un modulo equivalente en este backend.

### Recomendacion

No lo meteria dentro de este microservicio salvo que realmente quieras manejar mercaderia y archivo documental en el mismo dominio, lo cual normalmente no conviene.

La mejor opcion es:

- servicio separado,
- o modulo separado dentro del ERP,
- reutilizando el patron conceptual de barcode y ubicacion,
- pero no compartiendo el mismo modelo de negocio de inventario comercial.

En otras palabras: se puede reaprovechar la idea, no necesariamente el mismo bounded context.

---

## 5.9. Reportes auxiliares y BI

### Estado actual

El backend ya entrega consultas suficientes para stock, FEFO y kardex, pero no tiene una capa de reportes analiticos tipo ERP.

### Recomendacion

No sobrecargar el microservicio con reporteria pesada.

Mejor opciones:

- vistas de lectura optimizadas,
- servicio de reportes,
- ETL a BI,
- exportaciones desde un BFF.

El microservicio debe seguir siendo el dueno transaccional del inventario, no el motor principal de reporting corporativo.

---

## 5.10. Seguridad, tenant, auditoria y plataforma

Esto no es "paridad ERP" funcional, pero si condiciona cualquier integracion seria.

Hoy siguen faltando piezas importantes:

- autenticacion productiva real,
- tenant resuelto desde identidad, no desde header libre,
- auditoria con usuario real,
- idempotencia en comandos externos,
- outbox para integraciones,
- monitoreo y trazabilidad operativa.

Estas brechas deben tratarse como backlog de plataforma.

---

## 6. Que conviene mantener dentro de inventario y que no

| Tema | Donde deberia vivir |
| --- | --- |
| Contenedores, operaciones, stock, kardex | Inventario |
| Recepciones, ajustes, transferencias, picking, conteos, reservas | Inventario |
| Merma, max/min, snapshots de costo | Inventario |
| Productos maestro | Servicio de productos o catalogos |
| Empresas y tenants | IAM o maestro organizacional |
| Ordenes de compra | Compras |
| Pedidos, despacho, facturacion | Ventas / comercial |
| Asientos contables | Contabilidad |
| Etiquetas, PDF, layouts de impresion | Reportes / etiquetas |
| Inventario documental | Servicio o modulo aparte |

La regla general es simple:

- si cambia el stock fisico o la trazabilidad del contenedor, le pertenece a inventario;
- si solo referencia el stock desde otro proceso, debe integrarse, no absorberse aqui.

---

## 7. Modelo de integracion recomendado

## 7.1. Anti-Corruption Layer

Seguir el patron que el proyecto ya insinua:

- `ProductoAdapter`
- `ContabilidadAdapter`

Y agregar adapters equivalentes para:

- `ComprasAdapter`
- `VentasAdapter`
- `ProduccionAdapter`
- `EtiquetasAdapter` o `ReportesAdapter`

Cada adapter debe traducir el mundo externo al lenguaje propio de inventario.

---

## 7.2. Sync para validar y Async para propagar

### Integraciones sincronas

Usarlas para:

- validar existencia de producto,
- consultar linea de orden de compra,
- consultar estado de pedido o despacho,
- obtener datos minimos de referencia externa.

### Integraciones asincronas

Usarlas para:

- notificar recepcion,
- notificar salida de stock,
- notificar merma,
- notificar ajustes,
- disparar procesos contables,
- alimentar analytics o reporting.

---

## 7.3. Aprovechar lo que ya existe en el modelo

El backend ya tiene dos elementos muy utiles para integrar sin deformar el dominio:

1. Referencias externas en `Operacion`

- `tipoReferencia`
- `referenciaId`
- `referenciaLineaId`

2. Eventos internos ya publicados

- `InventarioRecibidoEvent`
- `PickingCompletadoEvent`
- `StockAjustadoEvent`
- `TransferenciaDespachadaEvent`
- `TransferenciaRecibidaEvent`
- `ConteoAplicadoEvent`

Hoy esos eventos son internos a Spring. El siguiente paso natural para integraciones reales es agregar un outbox y publicarlos hacia afuera.

---

## 7.4. Outbox e idempotencia

Si este servicio va a recibir comandos desde ventas, compras o produccion, necesitas robustez de integracion.

Recomiendo agregar:

- `idempotencyKey` por comando externo,
- tabla outbox para eventos salientes,
- reintentos controlados,
- estado de entrega de eventos,
- trazabilidad por `correlationId`.

Sin eso, las integraciones entre servicios quedan fragiles ante retries, timeouts o duplicados.

---

## 7.5. No compartir base de datos

Para llegar a una integracion senior:

- no compartir tablas entre inventario y ventas,
- no leer la BD de compras desde inventario,
- no hacer joins cross-service,
- no usar triggers en una BD comun como mecanismo de integracion.

La integracion debe ser por API y eventos, no por acoplamiento fisico de base de datos.

---

## 8. Roadmap sugerido

## Fase A. Cerrar lo interno que ya existe pero esta incompleto

- CRUD de configuracion de merma
- motivos de merma
- parametros por empresa:
  - expiracion default de reservas
  - dias alerta de vencimiento
  - formato de barcode
- seguridad y auditoria productiva
- idempotencia basica

## Fase B. Integracion con catalogos y compras

- implementacion real de `ProductoAdapter`
- `ComprasAdapter`
- recepcion contra orden de compra
- confirmacion de recepcion hacia compras

## Fase C. Integracion con ventas

- definir punto real de descuento:
  - reserva,
  - picking,
  - despacho,
  - factura
- `VentasAdapter`
- flujo comercial completo con referencias externas

## Fase D. Integracion contable y eventos externos

- outbox
- eventos salientes
- implementacion real de `ContabilidadAdapter`
- conciliacion contable basica

## Fase E. Satelites

- etiquetas / PDF / ZPL
- reportes auxiliares
- inventario documental, solo si realmente se necesita

---

## 9. Decisiones que conviene tomar antes de seguir

1. El descuento real de stock por venta va a ocurrir al facturar o al despachar.
2. Productos va a ser maestro externo o se quiere absorber dentro de este servicio.
3. Empresas se administraran en otro modulo o se agregara CRUD minimo aqui.
4. Inventario documental realmente pertenece al mismo sistema o debe ser otro dominio.
5. Las etiquetas y PDFs se van a generar aqui o en un modulo de reportes.
6. La merma automatica es una necesidad real o solo una herencia del ERP anterior.

---

## 10. Recomendacion final

No recomiendo intentar copiar 1:1 todo lo que existia alrededor del inventario dentro del ERP viejo.

La mejor ruta es:

- dejar este servicio como dueno del stock y la trazabilidad,
- cerrar sus brechas internas reales,
- integrar compras, ventas, productos y contabilidad como servicios vecinos,
- y mantener fuera de aqui los modulos que no cambian directamente el stock fisico.

Eso te da una arquitectura mas limpia, mas mantenible y mas senior que simplemente rehacer el ERP viejo dentro de un solo backend.
