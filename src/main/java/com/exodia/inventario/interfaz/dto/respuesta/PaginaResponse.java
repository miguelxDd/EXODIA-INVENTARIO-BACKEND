package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Respuesta paginada")
public record PaginaResponse<T>(
    @Schema(description = "Contenido de la pagina") List<T> contenido,
    @Schema(description = "Numero de pagina actual") int pagina,
    @Schema(description = "Tamanio de pagina") int tamanio,
    @Schema(description = "Total de elementos") long totalElementos,
    @Schema(description = "Total de paginas") int totalPaginas,
    @Schema(description = "Es primera pagina") boolean primera,
    @Schema(description = "Es ultima pagina") boolean ultima
) {
    public static <T> PaginaResponse<T> de(org.springframework.data.domain.Page<T> page) {
        return new PaginaResponse<>(
            page.getContent(), page.getNumber(), page.getSize(),
            page.getTotalElements(), page.getTotalPages(),
            page.isFirst(), page.isLast()
        );
    }
}
