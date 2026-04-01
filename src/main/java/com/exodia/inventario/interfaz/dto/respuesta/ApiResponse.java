package com.exodia.inventario.interfaz.dto.respuesta;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta estandar de la API")
public record ApiResponse<T>(
    @Schema(description = "Indica si la operacion fue exitosa") boolean exito,
    @Schema(description = "Mensaje descriptivo") String mensaje,
    @Schema(description = "Datos de la respuesta") T datos,
    @Schema(description = "Codigo de error si aplica") String codigoError,
    @Schema(description = "Timestamp de la respuesta") OffsetDateTime timestamp
) {
    public static <T> ApiResponse<T> exitoso(T datos, String mensaje) {
        return new ApiResponse<>(true, mensaje, datos, null, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> exitoso(T datos) {
        return exitoso(datos, "Operacion exitosa");
    }

    public static <T> ApiResponse<T> error(String codigoError, String mensaje) {
        return new ApiResponse<>(false, mensaje, null, codigoError, OffsetDateTime.now());
    }
}
