package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Linea de una recepcion")
public record RecepcionLineaResponse(
    @Schema(description = "ID de la linea") Long id,
    @Schema(description = "ID del contenedor creado") Long contenedorId,
    @Schema(description = "Codigo de barras del contenedor") String codigoBarras,
    @Schema(description = "ID del producto") Long productoId,
    @Schema(description = "ID de la unidad") Long unidadId,
    @Schema(description = "ID de la ubicacion") Long ubicacionId,
    @Schema(description = "Cantidad recibida") BigDecimal cantidad,
    @Schema(description = "Precio unitario") BigDecimal precioUnitario,
    @Schema(description = "Numero de lote") String numeroLote,
    @Schema(description = "Fecha de vencimiento") LocalDate fechaVencimiento,
    @Schema(description = "Barcode fue generado") Boolean barcodeGenerado,
    @Schema(description = "Barcode fue reutilizado") Boolean barcodeReutilizado
) {}
