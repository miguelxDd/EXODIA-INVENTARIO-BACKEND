package com.exodia.inventario.interfaz.rest.extension;

import com.exodia.inventario.aplicacion.comando.ValorizacionService;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.FotoCostoResponse;
import com.exodia.inventario.interfaz.dto.respuesta.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.exodia.inventario.util.InventarioConstantes.MAX_RESULTADOS_PAGINA;
import static com.exodia.inventario.util.InventarioConstantes.TAMANIO_PAGINA_DEFAULT;

@RestController
@RequestMapping("/api/v1/valorizacion")
@RequiredArgsConstructor
@Tag(name = "Valorizacion", description = "Valorizacion de inventario y fotos de costo")
public class ValorizacionController {

    private final ValorizacionService valorizacionService;

    @PostMapping("/foto-costo")
    @Operation(summary = "Generar foto de costo", description = "Genera una foto de costo del inventario actual")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Foto generada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<ApiResponse<Void>> generarFotoCosto(
            @RequestHeader("X-Empresa-Id") Long empresaId) {
        valorizacionService.generarFotoCosto(empresaId);
        return ResponseEntity.ok(ApiResponse.exitoso(null, "Foto de costo generada exitosamente"));
    }

    @GetMapping("/fotos-costo")
    @Operation(summary = "Listar fotos de costo", description = "Lista fotos de costo paginadas")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de fotos")
    })
    public ResponseEntity<ApiResponse<PaginaResponse<FotoCostoResponse>>> listarFotosCosto(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + TAMANIO_PAGINA_DEFAULT) int tamanio) {
        Pageable pageable = PageRequest.of(pagina, Math.min(tamanio, MAX_RESULTADOS_PAGINA));
        Page<FotoCostoResponse> page = valorizacionService.listarFotosCosto(empresaId, pageable);
        return ResponseEntity.ok(ApiResponse.exitoso(PaginaResponse.de(page)));
    }
}
