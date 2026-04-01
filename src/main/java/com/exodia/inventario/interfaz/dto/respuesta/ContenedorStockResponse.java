package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Stock de un contenedor")
public record ContenedorStockResponse(
    @Schema(description = "ID del contenedor") Long contenedorId,
    @Schema(description = "Codigo de barras") String codigoBarras,
    @Schema(description = "ID del producto") Long productoId,
    @Schema(description = "ID del proveedor") Long proveedorId,
    @Schema(description = "ID de la unidad") Long unidadId,
    @Schema(description = "ID de la bodega") Long bodegaId,
    @Schema(description = "ID de la ubicacion") Long ubicacionId,
    @Schema(description = "Precio unitario") BigDecimal precioUnitario,
    @Schema(description = "Numero de lote") String numeroLote,
    @Schema(description = "Fecha de vencimiento") LocalDate fechaVencimiento,
    @Schema(description = "Estado del contenedor") String estadoCodigo,
    @Schema(description = "Stock total") BigDecimal stockCantidad,
    @Schema(description = "Cantidad reservada") BigDecimal cantidadReservada,
    @Schema(description = "Cantidad disponible") BigDecimal cantidadDisponible
) {}
