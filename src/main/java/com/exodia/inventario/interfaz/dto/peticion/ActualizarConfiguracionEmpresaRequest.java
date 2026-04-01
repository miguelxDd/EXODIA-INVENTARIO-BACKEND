package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Peticion para actualizar configuracion de empresa")
public record ActualizarConfiguracionEmpresaRequest(
    @Schema(description = "Horas de expiracion de reserva") @Min(1) Integer expiracionReservaHoras,
    @Schema(description = "Dias de alerta de vencimiento") @Min(1) Integer diasAlertaVencimiento,
    @Schema(description = "Prefijo para codigos de barras") @Size(min = 1, max = 20) String barcodePrefijo,
    @Schema(description = "Longitud del padding del barcode") @Min(1) Integer barcodeLongitudPadding,
    @Schema(description = "Politica de salida: FEFO, FIFO o MANUAL") @Size(min = 1, max = 20) String politicaSalida
) {}
