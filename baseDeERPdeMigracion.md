# Investigacion del Modulo de Inventario de Bilans

## Estado del documento

Este documento es una investigacion tecnica del inventario actual del proyecto para poder replicarlo.

Objetivo de esta primera version:

- Explicar como esta construido el inventario hoy.
- Separar catalogos, movimientos, consultas, ajustes y reportes.
- Identificar las tablas y funciones que realmente sostienen el stock.
- Dejar claras las decisiones de diseno que conviene copiar y las que conviene corregir.

Este documento esta basado principalmente en estos archivos:

- `routes/partials/inventario.php`
- `app/Http/Controllers/Inventario/*.php`
- `app/Classes/Repository/InventarioRepository.php`
- `app/Models/Inventario/*.php`
- `app/helpers/Help.php`
- `database/backups/backup.sql`
- `database/migrations/2025_09_26_000000_create_inv_maximos_minimos_estados_table.php`
- `database/migrations/2025_09_26_000001_create_inv_maximos_minimos_table.php`
- `database/migrations/2026_02_26_102307_create_inv_transfer_product_details_table.php`
- `app/Http/Controllers/MermaConfigurationController.php`
- `app/Services/MermaService.php`
- `app/Http/Controllers/InvDocumentController.php`

---

## Matriz de cobertura

Esta matriz sirve como checklist rapido de lo ya investigado en este archivo.

| Modulo | Estado | Donde esta en este documento | Que hace en resultado | Recomendacion para replicarlo |
| --- | --- | --- | --- | --- |
| Contenedor trazable por barcode | Incluido | Secciones 1, 2, 3 y 22 | Define la unidad trazable real del inventario. Sobre este registro viven producto, proveedor, unidad, lote, vencimiento, bodega y ubicacion. | Replicarlo si o si. Es la base del sistema. |
| Kardex de operaciones | Incluido | Secciones 1, 2, 3 y 22 | Reconstruye el stock por suma de entradas y salidas, por barcode, producto o bodega. | Replicarlo si o si. No conviene volver a stock persistido por producto. |
| Bodegas | Incluido | Seccion 4.1 y mapa de funciones | Mantiene almacenes y crea infraestructura inicial para operar. | Replicarlo, pero limpiando nombres y reglas de creacion. |
| Ubicaciones por bodega | Incluido | Seccion 4.2 y mapa de funciones | Permite ubicar fisicamente el stock dentro de cada bodega y moverlo internamente. | Replicarlo. Es clave para orden operacional. |
| Unidades y conversiones | Incluido | Secciones 4.3, 4.4 y 8 | Soporta multiples unidades y conversion de stock total o parcial entre ellas. | Replicarlo, pero mover la logica a un servicio dedicado. |
| Recepcion general | Incluido | Seccion 6 y mapa de `RecepcionController` | Da entrada al inventario manual, por compra o por traslado. Crea contenedor y operacion positiva. | Replicarlo si o si. Es uno de los flujos nucleares. |
| Barcode manual y automatico | Incluido | Secciones 6.3, 6.4, 6.5 y 20.1 | Permite capturar barcode dado por usuario o generar uno interno para el contenedor. | Replicarlo, pero centralizando la politica de generacion. |
| Reutilizacion de barcode | Incluido | Seccion 6.4 | Reusa un barcode existente solo si coincide producto, proveedor, unidad, precio, lote, vencimiento, bodega y ubicacion. | Replicarlo solo si el negocio lo necesita. Si no, usar barcode estrictamente unico. |
| Consulta consolidada de inventario | Incluido | Seccion 7 y mapa de `ConsultaInventarioController` | Lista existencias agregadas por contenedor, con filtros por producto, bodega, proveedor, lote y barcode. | Replicarlo, pero separar mejor consulta por contenedor vs consulta por producto. |
| Conversion entre unidades | Incluido | Seccion 8 | Convierte inventario entre unidades; si es parcial, divide el contenedor y genera otro barcode. | Replicarlo. Es una fortaleza del modelo por contenedor. |
| Ajustes de inventario | Incluido | Seccion 9 | Corrige cantidades o precio sin romper la trazabilidad, dejando operaciones de ajuste. | Replicarlo, con catalogos mas claros de tipos y razones. |
| Movimientos internos | Incluido | Secciones 10 y 11 | Cambia ubicacion o asigna bodega/locacion a lotes que venian incompletos. | Replicarlo, eliminando hardcodes de standby. |
| Transferencia legacy entre bodegas por contenedor | Incluido | Seccion 12.1 y mapa de `TrasladoInventarioController` | Traslada barcodes concretos entre bodegas. Maneja salida, descarga y recepcion final por contenedor. | Replicarlo solo si quieres trazabilidad exacta por caja durante el traslado. |
| Transferencia nueva entre bodegas por producto | Incluido | Seccion 12.2 y mapa de `TrasladoInventarioController` | Traslada cantidades por producto sin seleccionar barcode al crear; los barcodes reales se resuelven al recibir. | Replicarlo mejorado, no tal cual. Hoy no reserva stock al crear el traslado. |
| Descarga y recepcion en destino | Incluido | Secciones 6.6, 12.1 y mapa de `DescargaBodegaController` | Controla que lo despachado llegue a destino y salga del estado de traslado/standby. | Replicarlo si mantendras flujo de embarque-descarga-recepcion. |
| Picking y requisiciones | Incluido | Seccion 13 | Reserva y descuenta inventario para solicitudes internas, consumos o preparacion de pedidos. | Replicarlo, pero unificando mejor los tipos de operacion. |
| Ajuste por ventas facturadas | Incluido | Seccion 14 | Relaciona ventas con barcodes descontados y descuenta inventario real por detalle vendido. | Replicarlo si el inventario debe cerrarse contra facturacion. |
| Inventario de costos | Incluido | Seccion 15.1 | Consolida existencias valorizadas para analisis de costo y costo por existencia. | Replicarlo si necesitas costeo operativo dentro del mismo modulo. |
| Inventario auxiliar | Incluido | Seccion 15.2 | Vista auxiliar de existencias para soporte operativo y consultas complementarias. | Replicarlo solo si aporta valor real; puede rehacerse como reporte. |
| Maximos y minimos | Incluido | Seccion 15.3 | Configura stock objetivo y alertas de reposicion por producto y bodega. | Replicarlo. Es de las extensiones mas utiles. |
| Merma | Incluido | Secciones 6.8, 16 y 20.9 | Registra perdida manual y contempla una arquitectura de merma automatica en recepcion. | Replicarlo mejorado. La merma manual si; la automatica no esta activa y debe redisenarse. |
| Inventario documental | Incluido | Seccion 17 y mapa de `InvDocumentController` | Usa el mismo patron de barcode y ubicacion para cajas documentales, no para mercaderia. | Replicarlo solo si tambien quieres trazabilidad documental. |
| Reportes PDF de barcode | Incluido | Secciones 17 y 20.1/20.10 | Imprime barcodes de inventario y de ubicaciones para operacion fisica. | Replicarlo si la operacion usa scanner e impresion de etiquetas. |
| Reglas de diseno y deuda tecnica | Incluido | Secciones 21, 22, 23 y 24 | Expone hardcodes, inconsistencias y decisiones que no conviene copiar tal cual. | Leer antes de implementar. Aqui esta la parte mas importante para no heredar deuda. |

### Lectura rapida para replicarlo

Si lo que quieres es clonar el comportamiento completo del inventario, los bloques obligatorios son estos:

- contenedor por barcode,
- kardex de operaciones,
- recepcion,
- consultas,
- ajustes,
- conversiones,
- movimientos,
- transferencias,
- picking o descuento operacional.

Si ademas quieres igualar el alcance total del sistema actual, entonces suma tambien:

- maximos y minimos,
- merma,
- ajuste por ventas,
- inventario de costos,
- inventario documental,
- impresion de barcodes.

---

## 1. Resumen ejecutivo

El inventario principal del sistema no esta basado en un "stock por producto" directo. Esta basado en dos piezas:

1. `inv_contenedor`
   Guarda la identidad fisica o logica del contenedor/caja/lote/barcode.

2. `inv_contenedor_operaciones`
   Guarda el kardex operacional.
   El stock se calcula como la suma de `cantidad` por barcode, producto, bodega o unidad segun la consulta.

En otras palabras:

- `inv_contenedor` = maestro del contenedor.
- `inv_contenedor_operaciones` = libro mayor de movimientos.
- `stock actual` = `SUM(cantidad)` del conjunto correcto de operaciones.

Ese es el centro del modulo. Todo lo demas gira alrededor de eso:

- recepcion crea contenedores y operaciones de entrada,
- picking crea operaciones negativas,
- traslado mueve stock creando salidas y entradas,
- ajuste agrega o descuenta operaciones,
- reportes reconstruyen existencias desde las operaciones.

### Lo mas importante para replicarlo

Si quieres replicar "un inventario como este", la pieza imprescindible es esta:

- tabla de contenedores con `barcode`, `producto`, `unidad`, `bodega`, `ubicacion`, `precio`, `lote`, `fecha_vencimiento`, proveedor y metadata;
- tabla de operaciones con `tipo_operacion`, `cantidad`, `barcode`, `fecha`, `bodega`, `ubicacion`, comentario y referencias;
- consultas que siempre calculan stock por suma, nunca por una columna `stock` persistida en el producto.

---

## 2. Modelo mental del inventario

## 2.1. Que representa un contenedor

Un contenedor no es solo una caja fisica. En este sistema representa una unidad trazable de inventario con:

- barcode unico o reutilizable segun configuracion,
- producto,
- proveedor,
- precio unitario,
- unidad de medida,
- bodega,
- localidad,
- lote,
- fecha de vencimiento,
- serie/origen/marca/garantia cuando aplica.

Tabla base: `inv_contenedor`.

Campos principales observados en `database/backups/backup.sql` y `app/Models/Inventario/InvContenedor.php`:

- `producto_proveedor_id`
- `producto_id`
- `proveedor_id`
- `precio`
- `barcode`
- `bodega_id`
- `localidad_id`
- `unidad_id`
- `fecha`
- `fecha_vencimiento`
- `serie`
- `origen_id`
- `garantia`
- `marca_id`
- `lote`
- `caja`
- `documento_id`

## 2.2. Que representa una operacion

Cada entrada, salida, ajuste o movimiento agrega un registro en `inv_contenedor_operaciones`.

Campos principales:

- `barcode`
- `locacion_id`
- `bodega_id`
- `producto_proveedor_id`
- `producto_id`
- `proveedor_id`
- `tipo_operacion_id`
- `cantidad`
- `tipo_ajuste_id`
- `comentario`
- `fecha`
- `lote`
- `caja`
- `fecha_vencimiento`
- `requisicion_id`
- `detalle_requisicion_id`
- `documento_id`

Regla de negocio central:

- cantidades positivas = entradas,
- cantidades negativas = salidas,
- cantidad `0` se usa para ajustes informativos como cambio de precio.

## 2.3. Como se calcula el stock

Formulas reales usadas en el codigo:

- stock de un barcode: `SUM(inv_contenedor_operaciones.cantidad) WHERE barcode = ?`
- stock de un producto en una bodega: `SUM(cantidad) WHERE producto_id = ? AND bodega_id = ?`
- stock por producto y unidad para maximos/minimos: `SUM(cantidad) WHERE producto_id = ? AND unidad_id = ?`

No hay una tabla maestra de stock consolidado persistido para el inventario principal.

---

## 3. Tablas principales del inventario

## 3.1. Catalogos estructurales

### `inv_bodega`

Mantiene bodegas.

Campos importantes:

- `bodega`
- `direccion`
- `ciudad`
- `pais`
- `ubicacion_standyby_id`
- `recepcion_temporal_ubicacion_id`
- `bodega_pro_terminado`
- `bodega_pro_consignacion`

Controlador: `BodegaController`

### `inv_locaciones_bodega`

Mantiene ubicaciones internas de cada bodega.

Campos importantes:

- `bodega_id`
- `ubicacion`
- `barcode`
- `estado`
- `standby`
- `tipo_localidad_id`

Controlador: `LocalidadBodegaController`

### `inv_unidad`

Catalogo de unidades de medida.

Ejemplos encontrados en backup:

- `Unidad`
- `Docena`
- `Caja`
- `Libra`
- `Onza`
- `Taza`
- `Galon`
- `Botella`
- `Kilogramo`

Controlador: `UnidadesController`

### `inv_conversiones`

Reglas de conversion entre unidades.

Campos:

- `unidad_origen_id`
- `unidad_destino_id`
- `factor_conversion`
- `tipo_operacion` (`multiplo` o `divisor`)
- `producto_id`

Controlador: `ConversionesController`

### `inv_tipo_operaciones`

Catalogo de tipos de movimiento.

Valores vistos en backup:

- `1 = Recepcion`
- `2 = Picking`
- `4 = Ajuste`
- `5 = Movimiento`
- `6 = Salida de contenedor`
- `7 = Entrada de contenedor`
- `8 = Entrada a stand by`
- `9 = Salida de stand by`
- `10 = Ingreso por Orden de Produccion`

### `inv_tipo_ajustes`

Catalogo de tipos de ajuste.

El sistema lo mantiene, pero en la practica muchas operaciones usan `tipo_ajuste_id = 4`, `6`, `7` o `8` como marcas internas.

### `inv_estado_traslado`

Estados del flujo de traslado legacy:

- `1 = Creado`
- `2 = Carga iniciada`
- `3 = Carga completada`
- `4 = Descarga iniciada`
- `5 = Descarga completada`
- `6 = Descarga terminada forzada`

## 3.2. Tablas operativas

### `inv_contenedor`

Maestro del contenedor.

### `inv_contenedor_operaciones`

Kardex/movimientos.

### `inv_traslados`

Cabecera de traslados.

Hoy soporta dos estilos:

- traslado legacy por contenedor,
- traslado nuevo por producto.

Campos nuevos agregados por migracion:

- `tipo`
- `transfer_number`

### `inv_traslados_contenedores`

Detalle del traslado legacy por barcode/contenedor.

Campos importantes:

- `traslado_id`
- `contenedor_id`
- `barcode`
- `cantidad`
- `is_unloading`
- `pick`

### `inv_transfer_product_details`

Detalle del traslado nuevo por producto.

Campos:

- `transfer_id`
- `product_id`
- `unit_id`
- `qty`
- `received`
- `received_qty`

### `inv_productos_facturados_estatus`

Tabla intermedia para ajuste de inventario contra ventas facturadas.

### `inv_productos_facturados_estatus_detalle`

Detalle de que barcode se desconto para cada venta.

### `inv_maximos_minimos`

Configuracion por producto y unidad para minimos y maximos.

### `inv_maximos_minimos_estados`

Estados:

- `1 = En Promedio`
- `2 = Bajo`
- `3 = Exceso`

---

## 4. Flujo base del inventario

El flujo operativo principal queda asi:

1. Se define la estructura:
   bodega -> ubicaciones -> unidades -> conversiones.
2. Se recibe inventario:
   se crea o reutiliza un contenedor y se registra una operacion positiva.
3. Se consulta stock:
   se agrupan operaciones por barcode o producto.
4. Se mueve o descuenta stock:
   se crean operaciones negativas y, si cambia de lugar, tambien positivas en el destino.
5. Se audita:
   los reportes reconstruyen todo desde operaciones.

---

## 5. Catalogos y mantenimiento

## 5.1. Bodegas

Archivo principal: `app/Http/Controllers/Inventario/BodegaController.php`

### Que hace

- lista bodegas,
- permite crear, editar y eliminar,
- genera ubicaciones automaticas al crear una bodega,
- distingue bodegas de producto terminado y consignacion,
- tiene exportacion/reportes adicionales.

### Como lo hace

Al crear una bodega (`store`):

- crea la fila en `inv_bodega`,
- crea una localidad standby:
  `"{bodega} STAND BY"`,
- si la bodega es de producto terminado:
  crea 5 localidades temporales:
  `"{bodega} TEMPORAL 1..5"`,
- guarda referencias:
  `ubicacion_standyby_id` y `recepcion_temporal_ubicacion_id`.

### Resultado funcional

Cada bodega nace con infraestructura minima para operar:

- una ubicacion standby,
- opcionalmente varias temporales para recepcion/produccion.

## 5.2. Localidades

Archivo principal: `app/Http/Controllers/Inventario/LocalidadBodegaController.php`

### Que hace

- administra ubicaciones por bodega,
- genera barcode para la ubicacion,
- impide borrar ubicaciones si tienen contenedores u operaciones asociadas,
- expone endpoints AJAX para cargar localidades por bodega.

### Resultado funcional

Las ubicaciones son escaneables y sirven tanto para mover contenedores como para pickear materiales.

## 5.3. Unidades

Archivo principal: `app/Http/Controllers/Inventario/UnidadesController.php`

### Que hace

- CRUD de unidades de medida,
- valida unicidad de codigo y nombre,
- impide borrar una unidad si esta siendo usada por contenedores o conversiones.

### Resultado funcional

El inventario puede convivir con varias unidades por producto y luego convertirlas.

## 5.4. Conversiones

Archivo principal: `app/Http/Controllers/Inventario/ConversionesController.php`

### Que hace

- CRUD de reglas de conversion,
- permite convertir en pantalla un valor usando factor y tipo (`multiplo` o `divisor`),
- amarra conversiones a producto cuando aplica.

### Resultado funcional

El sistema puede convertir, por ejemplo:

- caja -> unidad,
- libra -> onza,
- botella -> ml,

siempre que exista la regla.

---

## 6. Recepcion de inventario

Archivo principal: `app/Http/Controllers/Inventario/RecepcionController.php`

Vista principal: `resources/views/inventario/recepcion/recepcion-create.blade.php`

## 6.1. Modos de recepcion que existen

En la vista y en el controlador se observan varios escenarios:

- recepcion manual normal,
- recepcion por orden de compra,
- recepcion por traslado,
- recepcion con barcode generado automaticamente,
- recepcion con barcode manual,
- reutilizacion de barcode si la configuracion lo permite.

## 6.2. Datos que pide la pantalla

La vista trabaja con estos datos:

- proveedor,
- producto,
- bodega,
- localidad,
- unidad de medida,
- cantidad,
- precio,
- barcode o `generar_barcode`,
- lote y fecha de vencimiento opcionales,
- serie, garantia, origen, marca,
- tipo de recepcion,
- orden de compra si aplica,
- traslado y barcode origen si aplica.

Tambien usa AJAX para:

- cargar localidades por bodega,
- obtener cantidad disponible del producto en la bodega,
- cargar ordenes de compra,
- cargar productos y proveedores asociados,
- cargar traslados recibibles.

## 6.3. Recepcion manual normal

### Que hace

- crea un nuevo contenedor en `inv_contenedor`,
- crea una operacion positiva en `inv_contenedor_operaciones` con `tipo_operacion_id = 1`.

### Como lo hace

Pasos reales:

1. valida `location_id` y barcode,
2. busca relacion `pro_producto_proveedor`,
3. crea barcode si el usuario marco `generar_barcode`,
4. crea el contenedor con metadata,
5. crea la operacion de recepcion con comentario descriptivo,
6. confirma transaccion.

### Resultado funcional

El producto queda disponible en la bodega/localidad elegida y aparece en las consultas de inventario.

## 6.4. Reutilizacion de cajas/barcodes

La recepcion depende de la configuracion `reusar_caja`.

### Si `reusar_caja = 0`

- el barcode no puede repetirse,
- si ya existe, la recepcion se rechaza.

### Si `reusar_caja = 1`

El barcode si puede reutilizarse, pero solo si coinciden exactamente:

- producto,
- proveedor,
- bodega,
- unidad,
- relacion `producto_proveedor`.

Si todo coincide:

- no crea nuevo `inv_contenedor`,
- solo crea una nueva operacion positiva en `inv_contenedor_operaciones`,
- actualiza el precio del contenedor si cambio.

### Resultado funcional

El sistema puede manejar "misma caja reutilizable" sin duplicar maestros de contenedor.

## 6.5. Recepcion por orden de compra

Ademas de recibir inventario, el controlador actualiza trazabilidad de compras:

- marca `DetalleCompraVaria.inventario = true`,
- si todos los detalles de la orden ya pasaron a inventario:
  marca `OrdenCompra.finalizar_recepcion = true`.

### Resultado funcional

La orden de compra conoce que ya fue recepcionada en inventario.

## 6.6. Recepcion por traslado

Este es el flujo legacy de traslado.

### Que hace

Recibe un contenedor proveniente de otra bodega.

### Como lo hace

Si el traslado es parcial:

- clona el contenedor a un nuevo barcode en la bodega destino.

Si el traslado es total:

- reutiliza el mismo contenedor,
- cambia `bodega_id`, `localidad_id`, `precio` y `unidad_id`.

En ambos casos:

- crea salida de standby con `tipo_operacion_id = 9`,
- crea nueva entrada de recepcion con `tipo_operacion_id = 1`,
- marca `tipo_ajuste_id = 8` en la entrada para distinguir esa recepcion.

### Resultado funcional

El producto sale del estado standby del traslado y queda ya recibido en la ubicacion final.

## 6.7. Lote y vencimiento

La vista permite activar/desactivar lote con un checkbox.

Si el usuario marca `usar_lote`:

- `lote` y `fechaVencimiento` se vuelven obligatorios.

El controlador guarda esos datos tanto en `inv_contenedor` como en la operacion creada.

## 6.8. Merma automatica

El servicio existe (`MermaService`) y la recepcion esta preparada para llamarlo, pero en el codigo actual las llamadas estan comentadas.

Eso significa:

- la arquitectura de merma existe,
- la aplicacion automatica en recepcion hoy no esta activa desde este controlador.

---

## 7. Consultas de inventario

Controlador principal: `app/Http/Controllers/Inventario/InventarioController.php`

Repositorio real: `app/Classes/Repository/InventarioRepository.php`

## 7.1. Consulta general

Ruta:

- `inventario/consultar`

Vista:

- `resources/views/inventario/inventario/index_inventario.blade.php`

### Que hace

Muestra inventario consolidado por barcode con filtros por:

- fecha,
- bodega,
- localidad,
- proveedor,
- producto,
- barcode,
- lote.

### Como lo hace

Usa `InventarioRepository::consultasInventario($request, false)`.

La consulta:

- filtra operaciones,
- agrupa por `barcode`,
- conserva solo contenedores con `SUM(cantidad) > 0`,
- calcula reserva pendiente con:
  `SUM(qty) FROM reservar_pickeos WHERE barcode = ... AND picking = 0`.

### Resultado funcional

La pantalla muestra, por contenedor:

- producto,
- proveedor,
- bodega,
- localidad,
- barcode,
- precio,
- cantidad actual,
- total valorizado,
- cantidad reservada,
- cantidad disponible,
- lote,
- vencimiento.

Tambien permite:

- exportar PDF,
- exportar Excel,
- editar lote/vencimiento,
- ver detalle de reservas,
- lanzar conversion de unidad desde modal.

## 7.2. Consulta detallada de operaciones

Ruta:

- `inventario/detalle/consultar`

Vista:

- `resources/views/inventario/inventario/index_inventario_dt.blade.php`

### Que hace

Muestra el historial de movimientos del inventario.

### Como lo hace

Usa `InventarioRepository::consultasInventario($request, true)`.

Permite:

- fecha exacta,
- o rango de fechas,
- mismos filtros de bodega/proveedor/producto/barcode/lote.

### Resultado funcional

Se comporta como un kardex consultable por filtros.

## 7.3. Consulta por producto y bodega

Ruta:

- `inventario/consultar/producto/bodega`

Vista:

- `resources/views/inventario/inventario/index_inv_bodegaProducto.blade.php`

### Que hace

Consulta multiples productos contra multiples bodegas.

### Como lo hace

Usa `InventarioRepository::consultasInventarioProductoBodega`.

La vista soporta:

- seleccion multiple de productos,
- seleccion multiple de bodegas,
- "todos los productos",
- "todas las bodegas".

### Resultado funcional

Sirve como tablero comparativo de disponibilidad por producto/bodega.

### Nota tecnica importante

Aunque la pantalla se vende como "por producto y bodega", el repositorio sigue agrupando por `barcode`.
Es decir:

- funcionalmente sirve,
- pero tecnicamente sigue siendo una consulta por contenedor agregada, no un resumen puro por producto+bodega.

## 7.4. Edicion de lote y vencimiento

Metodo: `InventarioController::updateLoteFecha`

### Que hace

Actualiza ambos datos en:

- `inv_contenedor`,
- todas las filas de `inv_contenedor_operaciones` del mismo barcode.

### Resultado funcional

La metadata historica queda alineada con el contenedor.

---

## 8. Conversion de unidades en inventario

Metodo principal:

- `InventarioController::conversionUnidadesInventario`

## Que hace

Convierte una cantidad de un contenedor desde una unidad origen hacia otra unidad destino.

## Como lo hace

1. busca el contenedor,
2. busca la regla de conversion,
3. calcula stock actual del barcode,
4. valida que la cantidad a convertir no exceda el stock,
5. calcula el precio unitario resultante,
6. si se convierte todo el stock:
   reutiliza el mismo contenedor y le cambia la unidad,
7. si es conversion parcial:
   replica el contenedor y genera un nuevo barcode para la parte convertida,
8. crea una operacion negativa en el barcode origen con `tipo_operacion_id = 6`,
9. crea una operacion positiva en el barcode destino con `tipo_operacion_id = 7`.

## Resultado funcional

El inventario queda partido o transformado sin perder trazabilidad:

- sale cantidad en unidad origen,
- entra cantidad resultante en unidad destino,
- se conserva relacion con proveedor, lote y metadata.

---

## 9. Ajustes de inventario

Metodo principal:

- `InventarioController::storeAjusteInventario`

Vista:

- `resources/views/inventario/ajusteDeInventario.blade.php`

## 9.1. Que permite

La pantalla permite:

- filtrar contenedores,
- abrir modal de ajuste por barcode,
- ajustar en positivo,
- ajustar en negativo,
- ajuste mixto,
- asociar documento,
- generar partida contable,
- en ajuste negativo: generar nota de remision.

## 9.2. Como se hace el ajuste

El metodo:

1. localiza la ultima operacion del barcode,
2. calcula stock actual del barcode,
3. valida signo y suficiencia del ajuste,
4. crea una nueva fila en `inv_contenedor_operaciones` con `tipo_operacion_id = 4`,
5. deja comentario explicando ajuste, bodega, ubicacion y razon.

### Ajuste positivo

- `cantidad_ajuste > 0`

### Ajuste negativo

- `cantidad_ajuste < 0`
- valida que el stock no quede negativo.

### Ajuste mixto

Usa el mismo tipo de operacion pero permite positivo o negativo bajo una misma opcion de UI.

## 9.3. Integracion contable

Si el usuario selecciona cuenta contable:

- crea partida automatica con `Partidas::automatica(...)`,
- registra transaccion con `Partidas::transaccionesAjusteInventario(...)`.

El monto se calcula asi:

- `abs(cantidad_ajuste) * precio del contenedor`

## 9.4. Integracion con nota de remision

Si el ajuste es negativo y el usuario marca crear nota:

1. valida cliente,
2. crea `NotaRemision`,
3. agrega `NotaDeRemisionDetalle`,
4. opcionalmente guarda comentario,
5. intenta crear DTE con `FacturaElectronicaController::createDTENR(...)`.

## Resultado funcional

El ajuste puede ser:

- solo logistico,
- logistico + contable,
- logistico + documento de salida,
- logistico + contable + documento de salida.

## 9.5. Ajuste de precio

Metodo:

- `InventarioController::storeAjustePrecio`

### Que hace

- cambia `inv_contenedor.precio`,
- agrega operacion con `cantidad = 0` y `tipo_operacion_id = 4`.

### Resultado funcional

Hay trazabilidad del cambio de precio sin mover stock.

---

## 10. Movimiento de contenedores y standby

Controlador principal:

- `MovimientosController`

Repositorio que ejecuta el movimiento:

- `InventarioRepository::movimientoInventarioPorContenedor`

## 10.1. Movimiento hacia standby

Flujo:

- pantalla `movimiento_caja`,
- selecciona contenedor o grupo de contenedores,
- escanea o elige ubicacion standby destino,
- el repositorio crea dos operaciones:
  una salida del lugar actual y una entrada al standby,
- actualiza `localidad_id` y `bodega_id` del contenedor.

Tipos usados:

- salida de contenedor `6`,
- entrada a standby `8`.

## 10.2. Salida desde standby a ubicacion normal

Flujo inverso:

- pantalla `standby`,
- se valida que el contenedor realmente este en una ubicacion standby,
- el repositorio revierte el movimiento con:
  `9` salida de standby y `7` entrada de contenedor.

## 10.3. Movimiento por lote

Metodo:

- `MovimientosController::movimientoContenedorPorLote`

### Que hace

Asigna por lote una bodega y localidad a contenedores que venian sin asignacion.

### Como lo hace

- actualiza `inv_contenedor`,
- crea salida de standby con `9`,
- crea entrada a localidad final con `7`.

### Resultado funcional

Sirve para lotes recibidos inicialmente sin bodega definida.

---

## 11. Traslados de inventario

Hay dos sistemas de traslado coexistiendo.

Eso es clave para replicar correctamente el modulo.

## 11.1. Traslado legacy por contenedor

Controlador:

- `TrasladoInventarioController`

Tablas:

- `inv_traslados`
- `inv_traslados_contenedores`

### Flujo real

1. crear traslado con bodega origen y destino,
2. agregar barcodes al traslado,
3. cerrar traslado,
4. eso mueve contenedores de ubicacion normal a standby de salida,
5. luego la descarga/recepcion se completa por otro flujo.

### Cierre del traslado

Metodo:

- `cerrarTraslado`

Por cada item:

- crea salida `6`,
- crea entrada a standby `8`,
- cambia estado del traslado a `3`.

### Descarga

Controlador:

- `DescargaBodegaController`

Que hace:

- escanea barcodes de items del traslado,
- marca `pick = true`,
- cambia estados:
  `4` cuando empieza descarga,
  `5` cuando termina,
  `6` si se termina forzado.

### Recepcion final

La recepcion fisica en destino la hace `RecepcionController` en modo traslado.

### Resultado funcional

Este flujo simula embarque + descarga + recepcion por barcode.

## 11.2. Traslado nuevo por producto

Controlador:

- `TrasladoInventarioController`

Tabla de detalle:

- `inv_transfer_product_details`

Vista principal:

- `resources/views/inventario/transferencia/trasladoPorProducto.blade.php`

### Que hace

Permite trasladar productos por cantidad, sin tener que seleccionar barcodes manualmente.

### Como se crea

1. el usuario elige:
   bodega origen,
   bodega destino,
   unidad,
   fecha,
   comentario.
2. busca productos por AJAX con stock visible.
3. agrega lineas con cantidad.
4. al guardar:
   crea cabecera en `inv_traslados`,
   marca `tipo = 1`,
   genera `transfer_number`,
   crea detalles en `inv_transfer_product_details`.

Importante:

- en la creacion no se descuenta stock todavia.

### Como se recibe

Metodo:

- `storeRecibirTraslado`

Por cada detalle recibido:

1. valida stock actual en bodega origen,
2. llama `InvTraslado::salida(...)`,
3. esa funcion busca contenedores disponibles FIFO por `created_at`,
4. crea operaciones negativas sobre los barcodes origen,
5. devuelve el reparto por contenedor,
6. luego `InvTraslado::entrada(...)` crea nuevos contenedores en la bodega destino,
7. crea operaciones positivas con `tipo_operacion_id = 7`,
8. marca detalle como recibido.

### Resultado funcional

Este flujo hace el traslado "logico" por producto:

- consume stock origen usando varios contenedores si hace falta,
- crea contenedores nuevos en destino,
- conserva metadata del contenedor origen.

### Diferencia fuerte contra el flujo legacy

El traslado por producto no reserva ni descuenta al crear.
Descuenta realmente cuando se ejecuta la recepcion.

Eso implica que:

- el traslado puede haberse creado con un stock disponible,
- pero al recibirlo ese stock puede ya no existir,
- entonces el sistema valida de nuevo y puede rechazar la recepcion.

---

## 12. Picking y requisiciones

Controlador:

- `InvRequisicionesController`

## 12.1. Que cubre

Este modulo conecta inventario con requisiciones de materiales.

Tiene dos estilos:

- picking paso a paso por requisicion/detalle,
- picking multiple masivo,
- picking general desde una pantalla unificada.

## 12.2. Idea operativa

Una requisicion pide materiales.
El inventario responde descontando contenedores y marcando cuanto ya fue pickeado.

### Datos involucrados

- `ReqRequisicionMaterial`
- `ReqRequisionDetalle`
- `ReservarPickeo`
- `InvContenedorOperaciones`

## 12.3. Como elige el inventario

Se apoya mucho en `Help::existenciasDeProductoOrdenadoPorFechaVencimiento(...)`.

La intencion del diseno es clara:

- sugerir FIFO o FEFO,
- priorizar vencimiento mas cercano,
- localizar los barcodes/ubicaciones correctas.

## 12.4. Que hace cuando pickea

El flujo mas importante es este:

1. ubica el barcode a descontar,
2. incrementa `pick` en `ReqRequisionDetalle`,
3. crea una operacion negativa en `inv_contenedor_operaciones`,
4. liga la operacion a la requisicion mediante:
   `requisicion_id` y `detalle_requisicion_id`,
5. si todos los detalles quedaron completos:
   cambia estado de requisicion a `PROCESADA EN INVENTARIO`.

## 12.5. Picking multiple

Metodo:

- `pickMultiple`

### Que hace

Procesa varias lineas en una sola llamada AJAX.

### Resultado funcional

Permite cerrar una requisicion completa con menos pasos manuales.

## 12.6. Picking general

Metodo:

- `storePicking`

### Que hace

- valida reservas si existen,
- valida que el barcode pertenezca al material correcto,
- valida cantidad disponible,
- crea operacion negativa,
- actualiza `pick` en detalle,
- cierra requisicion si ya no falta nada.

### Resultado funcional

Sirve como picking asistido por escaneo.

## 12.7. Integracion con produccion

Si la requisicion viene de orden de produccion:

- cambia estados de orden de produccion,
- puede crear partida contable de consumo de materia prima si la configuracion lo habilita.

---

## 13. Ajuste de inventario por ventas facturadas

Controlador:

- `VentaAjusteController`

## Que problema resuelve

Cuando una venta ya esta facturada pero el descuento de inventario aun no fue materializado, este modulo construye la diferencia y permite sacar el inventario manualmente.

## Como lo hace

1. toma archivos/ventas facturadas por bodega activa,
2. si aun no existe detalle de ajuste:
   genera registros en `inv_productos_facturados_estatus`,
3. por cada detalle permite escanear un barcode compatible,
4. guarda en `inv_productos_facturados_estatus_detalle` cuanto se desconto y de que barcode,
5. crea operacion negativa en `inv_contenedor_operaciones`.

## Resultado funcional

Se cierra el hueco entre venta facturada y salida real de inventario.

---

## 14. Reportes y reconstruccion contable del inventario

## 14.1. Inventario de costos

Controlador:

- `InventarioController::inventarioDeCostoIndex`
- `InventarioController::reportInventarioDeCosto`

Repositorio:

- `InventarioRepository::reporteInventarioDeCosto`
- `InventarioRepository::costoInventario`
- `InventarioRepository::costoPromedio`

### Que hace

Valoriza inventario por producto.

### Como decide el costo

Depende de configuracion `costeo-fijo`:

- si esta activa, intenta usar costo fijo,
- si no, calcula costo promedio historico desde operaciones.

### Resultado funcional

Genera PDF o Excel de inventario valorizado.

## 14.2. Registro auxiliar de inventario

Controlador:

- `InventarioController::auxiliarInventarioIndex`
- `InventarioController::reportAuxiliarInventario`

Repositorio:

- `reportPdfAuxiliarInventario`
- `auxiliarInventario`
- `acumuladoAntesDe`

### Que hace

Reconstruye un kardex valorizado por producto entre fechas.

### Como lo hace

- calcula saldo inicial antes de la fecha,
- recorre operaciones en orden cronologico,
- recalcula costo promedio movil,
- separa entradas, salidas y saldos en unidades y monto.

### Tipos considerados como entrada

- `1`, `7`, `8`, `10`,
- ajustes positivos con tipo `4`.

### Tipos considerados como salida

- `2`, `5`, `6`, `9`,
- ajustes negativos con tipo `4`.

### Resultado funcional

Sirve como libro auxiliar de inventario y para trazabilidad contable.

## 14.3. Reporte F983

Metodo:

- `InventarioController::reporteF983`

Usa:

- `InvContenedorOperaciones::obtenerProductosInventario()`

Resultado:

- exporta CSV si hay datos.

---

## 15. Maximos y minimos

Controlador:

- `MaximosMinimosController`

Modelo:

- `InvMaximosMinimos`

## Que hace

Permite configurar por producto/unidad:

- stock actual,
- minimo,
- maximo,
- estado de stock.

## Como lo hace

Al crear o actualizar:

- calcula `stock_actual` con suma de operaciones del producto y unidad,
- determina estado:
  bajo / en promedio / exceso,
- guarda el resultado.

Tambien permite:

- actualizar un registro individual,
- actualizar todos,
- filtrar por estado,
- exportar el reporte.

## Resultado funcional

Actua como tablero de reabastecimiento.

---

## 16. Subsistema de merma

Controladores:

- `MermaConfigurationController`

Servicio:

- `MermaService`

Vistas:

- `inventario/merma/configuration.blade.php`
- `inventario/merma/manual.blade.php`
- `inventario/merma/statistics.blade.php`

## Que hace

Es una extension del inventario principal para registrar perdida/merma.

## Componentes funcionales

- configuracion general,
- porcentaje de merma por defecto,
- merma manual por contenedor,
- merma manual por producto con autodistribucion,
- simulacion de distribucion,
- validacion de stock por contenedor,
- estadisticas historicas,
- integracion contable.

## Como opera

El servicio hace cosas importantes:

- valida integridad del sistema,
- obtiene contenedores disponibles por producto,
- distribuye merma con estrategia FIFO,
- crea operaciones de inventario por merma,
- calcula costo promedio,
- genera partida contable cuando corresponde.

## Relacion con recepcion

La intencion del sistema es aplicar merma automaticamente al ingresar inventario.
Sin embargo, las llamadas desde `RecepcionController` estan comentadas.

Conclusiones practicas:

- el subsistema de merma existe,
- la merma manual si parece utilizable,
- la merma automatica en recepcion no esta activa en este punto del codigo.

---

## 17. Subsistema "Inventario de Documentos"

Controlador:

- `InvDocumentController`

Vistas:

- `inventario/inv-document/*`

## Que es

No es el mismo inventario de mercaderia basado en `inv_contenedor`.
Es otro inventario, especializado en cajas/documentos.

## Como esta construido

Tiene su propio conjunto de tablas/modelos:

- `InvDocumentCompany`
- `InvDocumentCategory`
- `InvDocumentType`
- `InvDocumentMunicipality`
- `InvDocumentPeriod`
- `InvDocumentProductType`
- `InvDocumentLocation`
- `InvDocumentContainer`
- `InvDocumentOperation`
- `InvDocumentContainerMovement`

## Operaciones que soporta

- alta de inventario documental,
- alta de catalogos auxiliares,
- impresion de barcodes,
- administracion de ubicaciones,
- ajuste de cantidad,
- movimiento de un barcode entre ubicaciones,
- movimiento masivo por ubicacion.

## Resultado funcional

Es un mini-WMS paralelo, pero para documentos/cajas, no para producto comercial.

## Recomendacion para replicar

No mezclarlo con el inventario principal si tu objetivo es replicar existencias de producto.
Replicalo solo si tambien quieres almacenar cajas documentales con barcode y ubicacion.

---

## 18. Integraciones externas del inventario

El inventario no vive aislado. Esta conectado con:

- compras,
- ordenes de compra,
- requisiciones,
- produccion,
- ventas facturadas,
- facturacion electronica,
- nota de remision,
- contabilidad,
- reservas de picking.

## Integraciones detectadas

### Compras / orden de compra

- recepcion marca detalles como pasados a inventario.

### Requisiciones / produccion

- picking descuenta inventario y puede disparar partidas contables.

### Facturacion electronica

- `InventoryOperationsSerivces` valida disponibilidad y hace picking automatico por producto o combo.

### Ajuste negativo

- puede crear Nota de Remision y DTE.

### Ventas ya facturadas

- `VentaAjusteController` resuelve descuentos pendientes.

---

## 19. Rutas funcionales importantes

Mapa compacto de rutas relevantes:

- `inventario/bodega`
- `inventario/locaciones`
- `inventario/unidad`
- `inventario/conversiones`
- `inventario/conver/convertir`
- `inventario/consultar`
- `inventario/detalle/consultar`
- `inventario/consultar/producto/bodega`
- `inventario/ajuste-inventario`
- `inventario/recepcion/create`
- `inventario/movimientos/contenedor`
- `inventario/movimientos/contenedor/temporal`
- `inventario/transferencia/inventario/traslado`
- `inventario/transferencia/listado`
- `inventario/transferencia/por-producto`
- `inventario/picking`
- `inventario/picking/requisiciones`
- `inventario/reporte-maximos-minimos`
- `/merma/*`
- `/inventory/document*`

---

## 20. Mapa de funciones por archivo

Esta seccion aterriza la investigacion a nivel de metodo, para que sepas exactamente donde vive cada comportamiento.

## 20.1. `InventarioController`

- `conversionUnidadesInventario`
  Convierte stock de un contenedor a otra unidad. Si la conversion es total reutiliza el contenedor; si es parcial clona el contenedor y genera otro barcode.
- `imprimir`
  Abre vista para impresion masiva de labels.
- `barcodesPdf`
  Genera PDF de barcodes.
- `barcode`
  Devuelve una tanda de barcodes nuevos.
- `getConversionesByUnidadInicio`
  Devuelve reglas de conversion a partir de una unidad origen.
- `consultaInventario`
  Consulta inventario consolidado por barcode usando el repositorio.
- `updateLoteFecha`
  Sincroniza lote y vencimiento en contenedor y operaciones.
- `getLocaciones`
  AJAX de localidades por bodega.
- `getProductosProveedor`
  AJAX de productos por proveedor.
- `consultaInventarioDetalle`
  Consulta el historial detallado de operaciones.
- `consultaInventarioPorProductoBodega`
  Consulta multi producto / multi bodega.
- `ajusteDeInventario`
  Renderiza pantalla de ajuste con filtros y listado de contenedores vivos.
- `storeAjusteInventario`
  Aplica ajuste de cantidad, asiento contable y nota de remision si aplica.
- `storeAjustePrecio`
  Cambia precio y deja huella en operaciones con cantidad cero.
- `reporteF983`
  Exporta CSV de inventario disponible.
- `detalleReservas`
  Devuelve reservas pendientes asociadas a un barcode.
- `inventarioDeCostoIndex`
  Muestra filtros del reporte de inventario valorizado.
- `reportInventarioDeCosto`
  Genera PDF o Excel de valorizacion.
- `auxiliarInventarioIndex`
  Muestra filtros del auxiliar de inventario.
- `reportAuxiliarInventario`
  Genera PDF o Excel del auxiliar.
- `maximosMinimosIndex`
  Muestra pantalla de reporte de maximos y minimos.
- `reportMaximosMinimos`
  Genera vista AJAX, PDF o Excel del reporte.

## 20.2. `InventarioRepository`

- `createPickingOperation`
  Genera una operacion negativa de picking para un barcode.
- `getContainerAvailableByProduct`
  Devuelve contenedores con stock disponible para un producto.
- `consultasInventarioProductoBodega`
  Consulta existencias agrupadas por barcode con filtros de producto/bodega.
- `consultasInventario`
  Consulta base del modulo; con `detalle=false` resume stock vivo y con `detalle=true` devuelve kardex.
- `consultasInventarioReq`
  Variante orientada a requisiciones/picking, incluyendo orden por vencimiento.
- `consultasInventarioAjuste`
  Variante usada para el listado previo al ajuste.
- `movimientoInventarioPorContenedor`
  Ejecuta doble movimiento entre ubicaciones o hacia/desde standby.
- `reporteInventarioDeCosto`
  Construye el PDF de valorizacion.
- `costoInventario`
  Calcula inventario valorizado por producto.
- `reportPdfAuxiliarInventario`
  Construye el PDF del auxiliar.
- `auxiliarInventario`
  Devuelve operaciones enriquecidas con cliente/proveedor/documento.
- `acumuladoAntesDe`
  Calcula saldo y costo promedio antes de una fecha.
- `costoPromedio`
  Recorre operaciones historicas y calcula promedio movil.

## 20.3. `RecepcionController`

- `getLocalidad`
  Devuelve solo ubicaciones generales de una bodega.
- `create`
  Renderiza pantalla de recepcion y carga catalogos.
- `storeContainer`
  Es el corazon de la recepcion: manual, por orden de compra o por traslado.
- `obtenerCantidadProductoBodega`
  AJAX con stock disponible del producto en una bodega.
- `getUnidadByProducto`
  Si la orden de compra ya define unidad, la devuelve; si no, devuelve todas.

## 20.4. `MovimientosController`

- `movimientoContenedor`
  Pantalla para mover uno o varios contenedores hacia standby de otra bodega.
- `movimientoContenedorPorLote`
  Asigna bodega/localidad a contenedores por lote y registra movimiento.
- `movimientoContenedorPorLoteGetLocalidad`
  AJAX de localidades para el flujo por lote.
- `movimientoContenedorTemporal`
  Pantalla para sacar contenedores de standby hacia una ubicacion normal.
- `storeMovimientoContenedor`
  Ejecuta movimiento normal.
- `storeMovimientoContenedorTemporal`
  Ejecuta movimiento inverso desde standby.

## 20.5. `TrasladoInventarioController`

- `index`
  Listado del flujo legacy por contenedor.
- `create`
  Pantalla para crear o editar cabecera del traslado legacy.
- `getContenedorTraslado`
  AJAX para validar barcode y stock disponible en la bodega origen.
- `store`
  Crea cabecera de traslado legacy.
- `agregarContenedorTraslado`
  Agrega un barcode al traslado legacy.
- `deleteItemTraslado`
  Quita un item del traslado legacy.
- `cerrarTraslado`
  Mueve barcodes a standby y deja el traslado listo para descarga.
- `getTraslados`
  Devuelve traslados completados/forzados para recepcion en destino.
- `getProveedoresTraslado`
  Obtiene proveedores presentes dentro de un traslado.
- `getProductosProveedorTraslado`
  Obtiene productos presentes dentro del traslado por proveedor.
- `getFieldsProductoProveedorTraslado`
  Devuelve primer barcode/cantidad compatible con proveedor+producto.
- `getBarcodesByTraslado`
  Devuelve barcodes del traslado.
- `getFieldsByBarcodeTraslado`
  Devuelve detalle completo de un barcode del traslado.
- `getUnidadesMedida`
  Devuelve unidades.
- `indexTrasladoPorProductos`
  Listado del flujo nuevo por producto.
- `createTrasladoPorProducto`
  Pantalla para capturar traslado por producto.
- `buscarProductosConStock`
  Busqueda AJAX de productos internos con stock.
- `storeTrasladoPorProducto`
  Crea cabecera y lineas del traslado por producto.
- `recibirTraslado`
  Pantalla de recepcion del traslado por producto.
- `storeRecibirTraslado`
  Ejecuta de verdad la salida en origen y entrada en destino.
- `detalleTraslado`
  Muestra lineas, stock disponible y estado del traslado por producto.
- `updateTrasladoPorProducto`
  Edita un traslado pendiente.
- `eliminarDetalleTraslado`
  Elimina una linea de detalle.
- `agregarDetalleTraslado`
  Agrega o incrementa una linea de detalle.

## 20.6. `InvRequisicionesController`

- `index`
  Picking guiado por requisicion y detalle.
- `indexListadoNoSelect`
  Variante masiva de la misma idea.
- `pickMultiple`
  Procesa varias lineas pickeadas en una sola llamada.
- `picking`
  Pantalla general de picking.
- `getMateriales`
  AJAX de materiales de una requisicion.
- `getLocations`
  AJAX que devuelve ubicaciones y barcodes reservados.
- `cerrarRequisicion`
  Cierra requisicion y libera reservas si corresponde.
- `getMaterialesDetails`
  Devuelve detalle cuantitativo de cada material.
- `storePicking`
  Hace el descuento real de inventario por barcode.
- `barcode`
  Valida si un barcode sirve para el material y devuelve cantidad utilizable.

## 20.7. `VentaAjusteController`

- `ajuste`
  Pantalla y flujo para descontar inventario a ventas facturadas.
- `calcularCantidadReal`
  Descompone una venta o combo en lineas pendientes de ajuste de inventario.

## 20.8. `MaximosMinimosController`

- `index`
  Lista configuraciones y estadisticas.
- `create`
  Pantalla de alta.
- `store`
  Crea configuracion y calcula estado inicial.
- `show`
  Detalle individual.
- `edit`
  Pantalla de edicion.
- `update`
  Actualiza unidad/min/max y recalcula estado.
- `destroy`
  Elimina configuracion.
- `actualizarStock`
  Relee stock desde operaciones para un registro.
- `actualizarTodosLosStocks`
  Relee stock para todos los registros.
- `indexData`
  Endpoint JSON para DataTables.

## 20.9. `MermaConfigurationController`

- `index`
  Configuracion general de merma.
- `updateConfiguration`
  Activa/desactiva merma y porcentaje por defecto.
- `statistics`
  Vista de estadisticas.
- `operationsData`
  DataTable de operaciones de merma.
- `processManualMerma`
  Procesa merma por contenedor o por producto.
- `manualMerma`
  Vista de captura manual.
- `getProductContainers`
  Devuelve contenedores disponibles para un producto.
- `simulateDistribution`
  Simula como se repartiria la merma.
- `validateContainerStock`
  Valida que un barcode soporte la merma solicitada.

## 20.10. `InvDocumentController`

- `index`
  Dashboard del inventario documental.
- `inventoryData`
  Consulta inventario documental filtrado.
- `addContainer`
  Pantalla de alta de caja/documento.
- `storeContainer`
  Crea contenedor documental y operacion inicial.
- `documentType`
  AJAX de tipos de documento por categoria.
- `storeCompanies`
  Alta rapida de empresas.
- `storeProductType`
  Alta rapida de tipo de producto documental.
- `storeMunicipalities`
  Alta rapida de municipios.
- `storePeriods`
  Alta rapida de periodos.
- `barcodesPdf`
  Imprime barcode de caja documental.
- `generateBarcodesPdf`
  Genera varios barcodes.
- `invDocumentLocation`
  Pantalla de ubicaciones documentales.
- `indexData`
  DataTable de ubicaciones documentales.
- `barcodesLocationPdf`
  Imprime barcode de ubicacion documental.
- `invDocumentLocationStore`
  Crea o actualiza ubicacion documental.
- `invDocumentLocationEdit`
  Devuelve datos de una ubicacion.
- `invDocumentLocationStatus`
  Activa o desactiva una ubicacion.
- `indexAdjustment`
  Pantalla de ajustes y movimientos documentales.
- `infoBarcode`
  Devuelve detalle de un barcode documental.
- `adjustmentQty`
  Ajusta cantidad de una caja documental.
- `movementBarcode`
  Mueve un barcode a otra ubicacion.
- `movementLocation`
  Mueve todos los barcodes de una ubicacion a otra.
- `barcodesInLocation`
  Lista barcodes contenidos en una ubicacion.

---

## 21. Riesgos, inconsistencias y deuda tecnica que no conviene copiar igual

Esta seccion es importante si de verdad lo vas a replicar.
Hay varias ideas buenas en el modulo, pero tambien varias cosas que yo no copiaria sin corregir.

## 21.1. Uso intensivo de "magic numbers"

El sistema depende mucho de IDs fijos:

- `1`, `4`, `6`, `7`, `8`, `9`, `10` para operaciones,
- `1` para ciertas ubicaciones,
- `1`, `2`, `3`, `5`, `6` para estados.

Problema:

- hace fragil el sistema,
- dificulta mantenimiento,
- genera errores cuando el catalogo cambia.

## 21.2. Inconsistencia en picking

Se detectan al menos dos criterios distintos:

- en `storePicking` usa `tipo_operacion_id = 2`,
- en `index/index2/pickMultiple` usa `tipo_operacion_id = 10` para descuento de requisiciones.

Ademas, en `InvTipoOperacion` existe:

- `const PICKING = 3`

pero el backup muestra:

- `2 = Picking`

Esto no esta alineado.

## 21.3. Standby hardcodeado

Parte del sistema usa `locacion_id = 1` como standby fijo.

Pero al mismo tiempo `inv_bodega` guarda `ubicacion_standyby_id`, lo que sugiere que cada bodega deberia tener su standby propio.

Eso indica mezcla de diseno viejo y nuevo.

## 21.4. Consulta por unidad no aplicada de forma consistente

`Help::existenciasDeProductoOrdenadoPorFechaVencimiento()` arma una request con `unidad`,
pero `InventarioRepository::consultasInventarioReq()` no aplica ese filtro.

Si replicas, corrige esto.

## 21.5. Traslado por producto descuenta al recibir, no al despachar

Esto tiene impacto operativo:

- el traslado creado no inmoviliza stock,
- el stock puede agotarse antes de recibir,
- el traslado puede fallar tarde.

Si quieres un flujo logistico mas robusto:

- reserva stock al crear,
- o descuenta al despachar,
- y solo confirma al recibir.

## 21.6. Costo fijo parece incompleto

En `InventarioRepository::costoInventario()` la consulta agrupa por `pc.costo`,
pero el `SELECT` no expone claramente ese campo.

Eso merece revision si se usa costeo fijo en produccion.

## 21.7. Recepcion y merma automatica desacopladas

La logica existe, pero esta comentada.

Si replicas:

- o la activas bien,
- o eliminas el acoplamiento parcial.

## 21.8. Mismo controlador para demasiadas responsabilidades

`InventarioController` mezcla:

- consulta,
- barcodes,
- ajustes,
- conversiones,
- reportes,
- maximos/minimos.

Eso vuelve mas dificil probarlo y mantenerlo.

## 21.9. Falta de una capa de dominio explicita

La logica critica esta repartida entre:

- controladores,
- repositorio,
- helper,
- modelos,
- servicios.

Si lo replicas desde cero, conviene concentrar reglas en servicios de dominio:

- `ReceiveInventoryService`
- `AdjustInventoryService`
- `TransferInventoryService`
- `PickInventoryService`
- `InventoryQueryService`

---

## 22. Que si conviene copiar

Estas ideas si son solidas y valen la pena:

## 22.1. Stock calculado por movimientos

Muy util para trazabilidad y auditoria.

## 22.2. Contenedor trazable por barcode

Permite:

- FIFO/FEFO,
- lote/vencimiento,
- serializacion parcial,
- movimientos muy precisos.

## 22.3. Separacion entre maestro y kardex

Es correcto separar:

- datos relativamente estables del contenedor,
- historial completo de operaciones.

## 22.4. Reportes reconstruidos desde operaciones

Es el camino correcto para:

- auxiliares,
- costo promedio,
- auditoria,
- conciliacion.

## 22.5. Integracion de inventario con compras, produccion y ventas

Esa parte refleja un modulo real, no un inventario aislado de laboratorio.

---

## 23. Como lo replicaria yo

Si el objetivo es replicar este inventario pero con una base mas limpia, haria esto.

## 23.1. Modelo minimo recomendado

### Tablas

- `warehouses`
- `warehouse_locations`
- `units`
- `unit_conversions`
- `inventory_containers`
- `inventory_operations`
- `inventory_transfer_headers`
- `inventory_transfer_lines`
- `inventory_reservations`
- `inventory_min_max`

### Catalogos auxiliares opcionales

- `operation_types`
- `adjustment_types`
- `transfer_statuses`

## 23.2. Reglas de dominio

- el stock nunca se edita directo;
- todo cambio entra por una operacion;
- un contenedor puede existir sin stock solo por trazabilidad historica;
- las consultas deben usar vistas o servicios optimizados para sumar movimientos;
- los traslados deben reservar o inmovilizar stock desde el despacho;
- las conversiones deben generar doble movimiento;
- los ajustes deben poder adjuntar motivo, documento y asiento contable.

## 23.3. Servicios minimos

- `ReceiveInventoryService`
- `MoveContainerService`
- `ConvertContainerUnitService`
- `AdjustContainerStockService`
- `TransferByContainerService`
- `TransferByProductService`
- `PickingService`
- `InventoryLedgerService`
- `InventoryValuationService`
- `MinMaxService`

## 23.4. Consultas que debes resolver bien

- stock por barcode,
- stock por producto,
- stock por producto+bodega,
- stock disponible menos reserva,
- kardex por barcode,
- kardex por producto,
- valorizacion por costo promedio,
- productos bajo minimo,
- trazabilidad por lote/vencimiento.

## 23.5. Si quieres copiar el resultado visual/funcional

Pantallas imprescindibles:

- mantenimiento de bodegas,
- mantenimiento de ubicaciones,
- unidades y conversiones,
- recepcion,
- consulta general,
- consulta de operaciones,
- ajuste de inventario,
- traslados,
- picking,
- inventario de costos,
- auxiliar inventario,
- maximos/minimos.

Pantallas opcionales:

- merma,
- ajuste por ventas facturadas,
- inventario de documentos.

---

## 24. Conclusiones finales

El inventario de Bilans no es un CRUD simple de productos.
Es un sistema de trazabilidad basado en contenedores y movimientos.

La mejor forma de entenderlo es asi:

- cada barcode representa una unidad trazable,
- cada movimiento agrega una linea al kardex,
- el stock no se guarda: se reconstruye,
- el sistema se apoya mucho en bodega, localidad, unidad, lote y fecha de vencimiento,
- varias funciones avanzadas se construyen encima de eso:
  picking, traslados, ajustes, auxiliares, maximos/minimos y merma.

Si quieres replicarlo fielmente:

- replica el modelo `contenedor + operaciones`,
- replica los flujos de recepcion, ajuste, traslado y picking,
- replica los reportes a partir de operaciones.

Si quieres replicarlo mejorado:

- conserva la idea central,
- corrige los magic numbers,
- unifica estados y tipos,
- mueve la logica a servicios de dominio,
- evita hardcodes como `locacion_id = 1`,
- define una politica clara de reserva/traslado.

---

## 25. Siguientes iteraciones sugeridas para este mismo documento

Si quieres, en la siguiente vuelta puedo ampliar este archivo con cualquiera de estas opciones:

- mapa exacto de tablas y relaciones en formato ER textual,
- listado metodo por metodo de cada controlador con su input/output,
- propuesta de base de datos nueva para replicarlo,
- propuesta de endpoints API para rehacerlo,
- propuesta de flujo UX pantalla por pantalla,
- lista de bugs actuales del modulo de inventario.