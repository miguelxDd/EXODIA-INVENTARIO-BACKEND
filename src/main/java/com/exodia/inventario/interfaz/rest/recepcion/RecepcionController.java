package com.exodia.inventario.interfaz.rest.recepcion;

import com.exodia.inventario.aplicacion.comando.RecepcionService;
import com.exodia.inventario.interfaz.dto.peticion.CrearRecepcionRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.PaginaResponse;
import com.exodia.inventario.interfaz.dto.respuesta.RecepcionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.exodia.inventario.util.InventarioConstantes.MAX_RESULTADOS_PAGINA;
import static com.exodia.inventario.util.InventarioConstantes.TAMANIO_PAGINA_DEFAULT;

@RestController
@RequestMapping("/api/v1/recepciones")
@RequiredArgsConstructor
@Tag(name = "Recepciones", description = "Recepcion de inventario")
public class RecepcionController {

    private final RecepcionService recepcionService;

    @PostMapping
    @Operation(summary = "Crear recepcion", description = "Crea una recepcion completa con contenedores y operaciones en el kardex")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recepcion creada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    })
    public ResponseEntity<ApiResponse<RecepcionResponse>> crear(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @Valid @RequestBody CrearRecepcionRequest request) {
        RecepcionResponse response = recepcionService.crear(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exitoso(response, "Recepcion creada exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener recepcion por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recepcion encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recepcion no encontrada")
    })
    public ResponseEntity<ApiResponse<RecepcionResponse>> obtenerPorId(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        RecepcionResponse response = recepcionService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping
    @Operation(summary = "Listar recepciones por empresa", description = "Lista recepciones paginadas")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de recepciones")
    })
    public ResponseEntity<ApiResponse<PaginaResponse<RecepcionResponse>>> listarPorEmpresa(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + TAMANIO_PAGINA_DEFAULT) int tamanio) {
        Pageable pageable = PageRequest.of(pagina, Math.min(tamanio, MAX_RESULTADOS_PAGINA));
        Page<RecepcionResponse> page = recepcionService.listarPorEmpresa(empresaId, pageable);
        return ResponseEntity.ok(ApiResponse.exitoso(PaginaResponse.de(page)));
    }
}
