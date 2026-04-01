package com.exodia.inventario.interfaz.rest.inventario;

import com.exodia.inventario.aplicacion.consulta.KardexQueryService;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.OperacionResponse;
import com.exodia.inventario.interfaz.dto.respuesta.PaginaResponse;
import com.exodia.inventario.interfaz.mapeador.OperacionMapeador;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

import static com.exodia.inventario.util.InventarioConstantes.MAX_RESULTADOS_PAGINA;
import static com.exodia.inventario.util.InventarioConstantes.TAMANIO_PAGINA_DEFAULT;

@RestController
@RequestMapping("/api/v1/inventario/kardex")
@RequiredArgsConstructor
@Tag(name = "Kardex", description = "Consultas de kardex (historial de operaciones)")
public class KardexController {

    private final KardexQueryService kardexQueryService;
    private final OperacionMapeador operacionMapeador;

    @GetMapping
    @Operation(summary = "Consultar kardex", description = "Consulta el historial de operaciones con filtros opcionales y paginacion")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Kardex paginado")
    })
    public ResponseEntity<ApiResponse<PaginaResponse<OperacionResponse>>> consultarKardex(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam(required = false) Long contenedorId,
            @RequestParam(required = false) String codigoBarras,
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) Long bodegaId,
            @RequestParam(required = false) OffsetDateTime fechaDesde,
            @RequestParam(required = false) OffsetDateTime fechaHasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + TAMANIO_PAGINA_DEFAULT) int tamanio) {
        Pageable pageable = PageRequest.of(pagina, Math.min(tamanio, MAX_RESULTADOS_PAGINA),
                Sort.by(Sort.Direction.DESC, "fechaOperacion"));
        Page<com.exodia.inventario.domain.modelo.contenedor.Operacion> page =
                kardexQueryService.consultarKardex(
                        empresaId, contenedorId, codigoBarras, productoId,
                        bodegaId, fechaDesde, fechaHasta, pageable);
        Page<OperacionResponse> responsePage = page.map(operacionMapeador::toResponse);
        return ResponseEntity.ok(ApiResponse.exitoso(PaginaResponse.de(responsePage)));
    }
}
