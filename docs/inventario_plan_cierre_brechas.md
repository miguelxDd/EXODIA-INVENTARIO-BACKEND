# Plan de Cierre de Brechas — Inventario vs ERP

## Objetivo

Cerrar las brechas entre el inventario legado descrito en `baseDeERPdeMigracion.md` y este microservicio sin volver a mezclar dominios que deben vivir separados.

La regla de trabajo es:

- cerrar dentro de inventario lo que afecta stock fisico, trazabilidad, ubicacion, reserva, conversion, conteo y merma;
- integrar via ACL + outbox lo que pertenece a compras, ventas, produccion o contabilidad;
- endurecer plataforma para que el servicio soporte produccion real.

## Estado despues de esta iteracion

Se cerraron o avanzaron estas brechas:

- movimientos internos de contenedores y uso explicito de `ubicacionStandby`
- conversion operativa de inventario por contenedor
- validacion sincronica de referencias externas para compras, ventas y produccion via adapters
- outbox persistente para eventos de negocio con reintentos basicos y `correlationId`
- relay y dispatcher base para integraciones asincronas
- eventos adicionales para merma, movimiento y conversion
- activacion de merma automatica opcional en recepcion via `cantidadMerma`
- motivo estructurado de merma para analitica y trazabilidad
- ajuste por venta facturada usando el agregado de `Ajuste`
- perfil `prod` con JWT resource server y validacion basica de tenant contra `X-Empresa-Id`
- resolucion automatica de `X-Empresa-Id` desde claims JWT en `prod`
- reglas mas seguras para movimientos: no desbloquear estados por mover y no mover contenedores con reservas activas
- reportes operativos de auxiliar/valorizacion actual y reimpresion basica de etiquetas en ZPL/SVG

Brechas que siguen abiertas pero ya con base tecnica para integrarse:

- implementaciones reales de `ComprasAdapter`, `VentasAdapter`, `ProduccionAdapter`, `ContabilidadAdapter`
- idempotencia completa con replay seguro de comandos
- cierre comercial final por despacho, dejando `ventas-ajustes` como excepcion o conciliacion
- inventario documental como bounded context aparte

## Fases recomendadas

## Fase 1. Cerrar inventario propio

- movimientos internos por ubicacion y standby
- conversion de unidad sobre stock vivo
- endurecer restricciones sobre reservas para conversiones y cambios de ubicacion
- completar pruebas verticales para movimientos y conversiones

## Fase 2. Integracion de recepciones

- implementar `ComprasAdapter` real
- recepcion contra orden de compra con validacion de pendientes
- integracion con produccion para ingresos de fabricacion
- confirmacion asincrona via outbox

## Fase 3. Integracion de salidas

- implementar `VentasAdapter` real
- definir punto tecnico del descuento:
  despacho, picking o venta facturada
- integrar picking y reservas con flujo comercial externo
- cerrar ajuste por venta solo como flujo de excepcion, no como camino principal

## Fase 4. Integracion financiera

- implementar `ContabilidadAdapter` real
- consumir eventos outbox desde relay productivo
- reintentos, dead-letter y monitoreo
- conciliacion basica entre inventario y contabilidad

## Fase 5. Plataforma

- idempotencia por comando externo
- observabilidad por `correlationId`
- seguridad productiva completa con claims de usuario y tenant
- auditoria enriquecida con identidad real

## Fase 6. Satelites

- evolucionar etiquetas a PDF/spool si la operacion realmente lo necesita
- reportes auxiliares y BI
- inventario documental solo si el negocio lo confirma como dominio aparte

## Que no debe absorber este servicio

- catalogo maestro completo de productos
- ordenes de compra
- pedidos, despacho comercial y facturacion
- asientos contables
- reporteria pesada
- inventario documental

## Criterio final

Si el flujo modifica stock, contenedor, ubicacion, lote o vencimiento, debe quedar aqui.

Si solo referencia ese stock desde otro proceso, debe integrarse por contrato y eventos.
