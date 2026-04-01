package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Linea de recepcion")
public record RecepcionLineaRequest(
    @NotNull @Schema(description = "ID del producto") Long productoId,
    @NotNull @Schema(description = "ID de la unidad") Long unidadId,
    @NotNull @Schema(description = "ID de la ubicacion destino") Long ubicacionId,
    @NotNull @Positive @Schema(description = "Cantidad a recibir") BigDecimal cantidad,
    @Schema(description = "Precio unitario") BigDecimal precioUnitario,
    @Size(max = 100) @Schema(description = "Numero de lote") String numeroLote,
    @Schema(description = "Fecha de vencimiento") LocalDate fechaVencimiento,
    @Schema(description = "ID del proveedor de la linea (override del header)") Long proveedorId,
    @Size(max = 100) @Schema(description = "Codigo de barras existente (para reutilizar contenedor)") String codigoBarras
) {}
