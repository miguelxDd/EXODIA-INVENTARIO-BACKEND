package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Respuesta de configuracion de empresa")
public record ConfiguracionEmpresaResponse(
    @Schema(description = "ID") Long id,
    @Schema(description = "ID de la empresa") Long empresaId,
    @Schema(description = "Horas de expiracion de reserva") Integer expiracionReservaHoras,
    @Schema(description = "Dias de alerta de vencimiento") Integer diasAlertaVencimiento,
    @Schema(description = "Prefijo para codigos de barras") String barcodePrefijo,
    @Schema(description = "Longitud del padding del barcode") Integer barcodeLongitudPadding,
    @Schema(description = "Politica de salida") String politicaSalida,
    @Schema(description = "Fecha de modificacion") OffsetDateTime modificadoEn
) {}
