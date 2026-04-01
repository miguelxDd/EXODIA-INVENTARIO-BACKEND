package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Etiqueta operativa lista para reimpresion")
public record EtiquetaResponse(
        @Schema(description = "Tipo de etiqueta") String tipoEtiqueta,
        @Schema(description = "ID de la entidad etiquetada") Long entidadId,
        @Schema(description = "Codigo de barras a imprimir") String codigoBarras,
        @Schema(description = "Titulo principal") String titulo,
        @Schema(description = "Subtitulo descriptivo") String subtitulo,
        @Schema(description = "Detalles visibles en la etiqueta") List<String> detalles,
        @Schema(description = "Plantilla ZPL generada") String zpl,
        @Schema(description = "Vista previa SVG simplificada") String svgVistaPrevia
) {}
